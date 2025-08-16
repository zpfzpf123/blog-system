from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, AnyHttpUrl
import requests
from bs4 import BeautifulSoup
from urllib.parse import urljoin, urlparse

app = FastAPI(title="Website Scraper Service")


class ScrapeRequest(BaseModel):
    url: AnyHttpUrl


class ScrapeResponse(BaseModel):
    success: bool
    title: str | None = None
    description: str | None = None
    keywords: str | None = None
    favicon: str | None = None
    error: str | None = None


UA = {
    "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
}


def get_origin(url: str) -> str:
    u = urlparse(url)
    netloc = f"{u.hostname}" + (f":{u.port}" if u.port else "")
    scheme = u.scheme or "https"
    return f"{scheme}://{netloc}"


def score_sizes(sizes: str | None) -> int:
    if not sizes:
        return 0
    try:
        best = 0
        for token in sizes.split():
            if "x" in token:
                parts = token.lower().split("x")
                w = int("".join([c for c in parts[0] if c.isdigit()]))
                h = int("".join([c for c in parts[1] if c.isdigit()]))
                diff = abs(w - 32) + abs(h - 32)
                score = 100 - diff
                best = max(best, score)
        return best
    except Exception:
        return 0


def find_best_favicon(soup: BeautifulSoup, page_url: str) -> str | None:
    priority = [
        "apple-touch-icon",
        "apple-touch-icon-precomposed",
        "mask-icon",
        "icon",
        "shortcut icon",
    ]
    for rel in priority:
        best_href = None
        best_score = -1
        for link in soup.find_all("link"):
            rel_attr = link.get("rel") or link.get("rel", [])
            if isinstance(rel_attr, list):
                rel_values = " ".join(rel_attr).lower()
            else:
                rel_values = str(rel_attr).lower()
            if rel not in rel_values:
                continue
            href = link.get("href")
            if not href:
                continue
            sizes = link.get("sizes")
            score = score_sizes(sizes)
            abs_href = urljoin(page_url, href)
            if score > best_score:
                best_score = score
                best_href = abs_href
        if best_href:
            return best_href
    return urljoin(get_origin(page_url), "/favicon.ico")


@app.post("/scrape", response_model=ScrapeResponse)
def scrape(req: ScrapeRequest):
    try:
        resp = requests.get(str(req.url), headers=UA, timeout=10, allow_redirects=True)
        resp.raise_for_status()
        soup = BeautifulSoup(resp.text, "lxml")

        title = soup.title.string.strip() if soup.title and soup.title.string else None
        desc_meta = soup.find("meta", attrs={"name": "description"})
        description = desc_meta.get("content").strip() if desc_meta and desc_meta.get("content") else None
        kw_meta = soup.find("meta", attrs={"name": "keywords"})
        keywords = kw_meta.get("content").strip() if kw_meta and kw_meta.get("content") else None
        favicon = find_best_favicon(soup, resp.url)

        return ScrapeResponse(success=True, title=title, description=description, keywords=keywords, favicon=favicon)
    except Exception as e:
        return ScrapeResponse(success=False, error=str(e))


# 运行： uvicorn main:app --host 0.0.0.0 --port 8001

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)

