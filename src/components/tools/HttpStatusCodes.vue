<template>
  <div class="tool-content">
    <div class="search-box">
      <input v-model="search" placeholder="搜索状态码、英文名称、中文名称或描述..." />
    </div>
    <div class="status-list">
      <div v-for="group in filteredGroups" :key="group.name" class="status-group">
        <h4 :class="group.class">{{ group.name }}</h4>
        <div v-for="code in group.codes" :key="code.code" class="status-item" @click="selected = code">
          <span class="code" :class="group.class">{{ code.code }}</span>
          <span class="name">{{ code.name }}</span>
          <span class="name-cn">{{ code.nameCn }}</span>
        </div>
      </div>
    </div>
    <div v-if="selected" class="detail-panel">
      <div class="detail-header">
        <span class="detail-code" :class="getCodeClass(selected.code)">{{ selected.code }}</span>
        <div class="detail-titles">
          <h3>{{ selected.name }}</h3>
          <span class="cn-title">{{ selected.nameCn }}</span>
        </div>
      </div>
      <div class="detail-section">
        <div class="section-title">📋 描述 / Description</div>
        <p class="desc-text">{{ selected.desc }}</p>
        <p class="desc-text-en">{{ selected.descEn }}</p>
      </div>
      <div class="detail-section">
        <div class="section-title">📦 响应结果 / Response Result</div>
        <p class="response-text">{{ selected.response }}</p>
        <p class="response-text-en">{{ selected.responseEn }}</p>
      </div>
      <div class="detail-section">
        <div class="section-title">🔧 处理办法 / Solution</div>
        <ul class="solution-list">
          <li v-for="(solution, idx) in selected.solutions" :key="idx">
            <span class="solution-cn">{{ solution.cn }}</span>
            <span class="solution-en">{{ solution.en }}</span>
          </li>
        </ul>
      </div>
      <div v-if="selected.example" class="detail-section">
        <div class="section-title">💡 示例场景 / Example</div>
        <p class="example-text">{{ selected.example }}</p>
        <p class="example-text-en">{{ selected.exampleEn }}</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'

interface StatusCode {
  code: number
  name: string
  nameCn: string
  desc: string
  descEn: string
  response: string
  responseEn: string
  solutions: { cn: string; en: string }[]
  example?: string
  exampleEn?: string
}

interface StatusGroup {
  name: string
  class: string
  codes: StatusCode[]
}

const search = ref('')
const selected = ref<StatusCode | null>(null)

const getCodeClass = (code: number): string => {
  if (code < 200) return 'info'
  if (code < 300) return 'success'
  if (code < 400) return 'redirect'
  if (code < 500) return 'client-error'
  return 'server-error'
}

const statusGroups: StatusGroup[] = [
  { 
    name: '1xx 信息响应 (Informational)', 
    class: 'info', 
    codes: [
      { 
        code: 100, 
        name: 'Continue', 
        nameCn: '继续', 
        desc: '服务器已收到请求头，客户端应继续发送请求体。',
        descEn: 'The server has received the request headers and the client should proceed to send the request body.',
        response: '服务器返回空响应体，仅包含状态行。',
        responseEn: 'Server returns an empty response body with only the status line.',
        solutions: [
          { cn: '客户端继续发送请求体数据', en: 'Client should continue sending the request body' },
          { cn: '确保请求头中包含 Expect: 100-continue', en: 'Ensure the request header contains Expect: 100-continue' }
        ],
        example: '上传大文件时的预检请求',
        exampleEn: 'Pre-check when uploading large files'
      },
      { 
        code: 101, 
        name: 'Switching Protocols', 
        nameCn: '切换协议', 
        desc: '服务器同意切换到客户端请求的协议。',
        descEn: 'The server agrees to switch to the protocol requested by the client.',
        response: '服务器返回 Upgrade 头，指示新协议。',
        responseEn: 'Server returns Upgrade header indicating the new protocol.',
        solutions: [
          { cn: '客户端按新协议继续通信', en: 'Client should continue communication using the new protocol' },
          { cn: '常用于 WebSocket 连接建立', en: 'Commonly used for WebSocket connection establishment' }
        ],
        example: 'WebSocket 握手升级',
        exampleEn: 'WebSocket handshake upgrade'
      },
      { 
        code: 102, 
        name: 'Processing', 
        nameCn: '处理中', 
        desc: '服务器已收到请求并正在处理，但尚无响应可用。',
        descEn: 'The server has received the request and is processing it, but no response is available yet.',
        response: '服务器返回空响应，表示请求正在处理。',
        responseEn: 'Server returns empty response indicating the request is being processed.',
        solutions: [
          { cn: '客户端等待最终响应', en: 'Client should wait for the final response' },
          { cn: '可设置合理的超时时间', en: 'Set a reasonable timeout period' }
        ],
        example: 'WebDAV 长时间操作',
        exampleEn: 'WebDAV long-running operations'
      },
      { 
        code: 103, 
        name: 'Early Hints', 
        nameCn: '早期提示', 
        desc: '服务器在最终响应前发送一些响应头，用于预加载资源。',
        descEn: 'Server sends some response headers before the final response for preloading resources.',
        response: '返回 Link 头，指示可预加载的资源。',
        responseEn: 'Returns Link headers indicating resources that can be preloaded.',
        solutions: [
          { cn: '浏览器可提前加载指定资源', en: 'Browser can preload specified resources' },
          { cn: '优化页面加载性能', en: 'Optimize page loading performance' }
        ],
        example: '预加载 CSS、JS 资源',
        exampleEn: 'Preloading CSS and JS resources'
      }
    ]
  },
  { 
    name: '2xx 成功 (Success)', 
    class: 'success', 
    codes: [
      { 
        code: 200, 
        name: 'OK', 
        nameCn: '成功', 
        desc: '请求成功，服务器返回请求的数据。',
        descEn: 'The request succeeded and the server returns the requested data.',
        response: '返回请求的资源数据，格式取决于 Content-Type。',
        responseEn: 'Returns the requested resource data, format depends on Content-Type.',
        solutions: [
          { cn: '正常处理返回的数据', en: 'Process the returned data normally' },
          { cn: '检查响应数据格式是否符合预期', en: 'Check if the response data format meets expectations' }
        ],
        example: 'GET 请求成功返回数据',
        exampleEn: 'GET request successfully returns data'
      },
      { 
        code: 201, 
        name: 'Created', 
        nameCn: '已创建', 
        desc: '请求成功并创建了新资源。',
        descEn: 'The request succeeded and a new resource was created.',
        response: '返回新创建资源的信息，通常包含 Location 头指向新资源。',
        responseEn: 'Returns information about the newly created resource, usually includes Location header pointing to the new resource.',
        solutions: [
          { cn: '获取 Location 头中的新资源地址', en: 'Get the new resource URL from Location header' },
          { cn: '更新本地数据或跳转到新资源', en: 'Update local data or navigate to the new resource' }
        ],
        example: 'POST 创建新用户成功',
        exampleEn: 'POST successfully creates a new user'
      },
      { 
        code: 202, 
        name: 'Accepted', 
        nameCn: '已接受', 
        desc: '请求已被接受处理，但处理尚未完成。',
        descEn: 'The request has been accepted for processing, but the processing has not been completed.',
        response: '返回任务状态信息或任务ID，用于后续查询。',
        responseEn: 'Returns task status information or task ID for subsequent queries.',
        solutions: [
          { cn: '轮询或使用 WebSocket 获取处理结果', en: 'Poll or use WebSocket to get processing results' },
          { cn: '保存任务ID用于后续状态查询', en: 'Save task ID for subsequent status queries' }
        ],
        example: '异步任务提交成功',
        exampleEn: 'Async task submitted successfully'
      },
      { 
        code: 203, 
        name: 'Non-Authoritative Information', 
        nameCn: '非权威信息', 
        desc: '返回的信息来自第三方，而非原始服务器。',
        descEn: 'The returned information is from a third party, not the origin server.',
        response: '返回可能被代理修改过的数据。',
        responseEn: 'Returns data that may have been modified by a proxy.',
        solutions: [
          { cn: '注意数据可能不是最新的', en: 'Note that data may not be the latest' },
          { cn: '如需权威数据，直接请求源服务器', en: 'Request the origin server directly for authoritative data' }
        ],
        example: '通过代理获取的缓存数据',
        exampleEn: 'Cached data obtained through proxy'
      },
      { 
        code: 204, 
        name: 'No Content', 
        nameCn: '无内容', 
        desc: '请求成功但无返回内容。',
        descEn: 'The request succeeded but there is no content to return.',
        response: '响应体为空，仅返回响应头。',
        responseEn: 'Response body is empty, only response headers are returned.',
        solutions: [
          { cn: '不需要处理响应体', en: 'No need to process response body' },
          { cn: '根据业务逻辑更新UI状态', en: 'Update UI state according to business logic' }
        ],
        example: 'DELETE 删除成功',
        exampleEn: 'DELETE operation successful'
      },
      { 
        code: 205, 
        name: 'Reset Content', 
        nameCn: '重置内容', 
        desc: '请求成功，客户端应重置文档视图。',
        descEn: 'The request succeeded and the client should reset the document view.',
        response: '响应体为空，指示客户端重置表单。',
        responseEn: 'Response body is empty, indicating client should reset the form.',
        solutions: [
          { cn: '清空表单或重置页面状态', en: 'Clear form or reset page state' },
          { cn: '刷新当前视图', en: 'Refresh current view' }
        ],
        example: '表单提交后重置',
        exampleEn: 'Reset after form submission'
      },
      { 
        code: 206, 
        name: 'Partial Content', 
        nameCn: '部分内容', 
        desc: '服务器返回部分内容，响应 Range 请求。',
        descEn: 'The server returns partial content in response to a Range request.',
        response: '返回 Content-Range 头和请求范围内的数据。',
        responseEn: 'Returns Content-Range header and data within the requested range.',
        solutions: [
          { cn: '继续请求剩余部分完成下载', en: 'Continue requesting remaining parts to complete download' },
          { cn: '合并所有部分数据', en: 'Merge all partial data' }
        ],
        example: '断点续传、视频流播放',
        exampleEn: 'Resume download, video streaming'
      },
      { 
        code: 207, 
        name: 'Multi-Status', 
        nameCn: '多状态', 
        desc: '响应体包含多个独立操作的状态信息。',
        descEn: 'The response body contains status information for multiple independent operations.',
        response: '返回 XML 格式的多状态响应。',
        responseEn: 'Returns multi-status response in XML format.',
        solutions: [
          { cn: '解析每个操作的独立状态', en: 'Parse the independent status of each operation' },
          { cn: '分别处理成功和失败的操作', en: 'Handle successful and failed operations separately' }
        ],
        example: 'WebDAV 批量操作结果',
        exampleEn: 'WebDAV batch operation results'
      },
      { 
        code: 208, 
        name: 'Already Reported', 
        nameCn: '已报告', 
        desc: 'DAV 绑定的成员已在之前的响应中列出。',
        descEn: 'The members of a DAV binding have already been enumerated in a previous response.',
        response: '避免重复列出相同的资源。',
        responseEn: 'Avoids repeatedly listing the same resources.',
        solutions: [
          { cn: '参考之前的响应获取完整信息', en: 'Refer to previous response for complete information' },
          { cn: '用于 WebDAV 的递归操作', en: 'Used for WebDAV recursive operations' }
        ],
        example: 'WebDAV 递归属性查询',
        exampleEn: 'WebDAV recursive property query'
      },
      { 
        code: 226, 
        name: 'IM Used', 
        nameCn: '使用了实例操作', 
        desc: '服务器已完成资源的 GET 请求，响应是对当前实例应用的一个或多个实例操作的结果。',
        descEn: 'The server has fulfilled a GET request for the resource, and the response is a representation of the result of one or more instance-manipulations applied to the current instance.',
        response: '返回经过增量编码的响应。',
        responseEn: 'Returns delta-encoded response.',
        solutions: [
          { cn: '客户端需支持增量编码', en: 'Client needs to support delta encoding' },
          { cn: '用于优化带宽使用', en: 'Used to optimize bandwidth usage' }
        ],
        example: 'HTTP 增量编码',
        exampleEn: 'HTTP delta encoding'
      }
    ]
  },
  { 
    name: '3xx 重定向 (Redirection)', 
    class: 'redirect', 
    codes: [
      { 
        code: 300, 
        name: 'Multiple Choices', 
        nameCn: '多种选择', 
        desc: '请求的资源有多个可选版本。',
        descEn: 'The requested resource has multiple representations available.',
        response: '返回可选资源列表，可能包含 Location 头。',
        responseEn: 'Returns a list of available resources, may include Location header.',
        solutions: [
          { cn: '让用户选择合适的资源版本', en: 'Let user choose the appropriate resource version' },
          { cn: '根据 Accept 头自动选择', en: 'Auto-select based on Accept header' }
        ],
        example: '多语言版本页面选择',
        exampleEn: 'Multi-language page selection'
      },
      { 
        code: 301, 
        name: 'Moved Permanently', 
        nameCn: '永久移动', 
        desc: '资源已永久移动到新位置。',
        descEn: 'The resource has been permanently moved to a new location.',
        response: '返回 Location 头指向新地址，浏览器会自动跳转。',
        responseEn: 'Returns Location header pointing to new address, browser will redirect automatically.',
        solutions: [
          { cn: '更新书签和链接到新地址', en: 'Update bookmarks and links to new address' },
          { cn: '搜索引擎会更新索引', en: 'Search engines will update their index' },
          { cn: '后续请求应使用新地址', en: 'Subsequent requests should use the new address' }
        ],
        example: '网站域名更换、URL 结构调整',
        exampleEn: 'Domain change, URL structure adjustment'
      },
      { 
        code: 302, 
        name: 'Found', 
        nameCn: '临时移动', 
        desc: '资源临时移动到其他位置。',
        descEn: 'The resource is temporarily located at a different location.',
        response: '返回 Location 头指向临时地址。',
        responseEn: 'Returns Location header pointing to temporary address.',
        solutions: [
          { cn: '跟随重定向访问临时地址', en: 'Follow redirect to temporary address' },
          { cn: '保留原地址用于后续请求', en: 'Keep original address for subsequent requests' },
          { cn: '注意：某些客户端可能改变请求方法', en: 'Note: Some clients may change request method' }
        ],
        example: '临时跳转到维护页面',
        exampleEn: 'Temporary redirect to maintenance page'
      },
      { 
        code: 303, 
        name: 'See Other', 
        nameCn: '查看其他', 
        desc: '应使用 GET 方法获取另一个 URI 的响应。',
        descEn: 'The response to the request can be found under another URI using GET method.',
        response: '返回 Location 头，客户端应使用 GET 请求新地址。',
        responseEn: 'Returns Location header, client should use GET to request new address.',
        solutions: [
          { cn: '使用 GET 方法请求新地址', en: 'Use GET method to request new address' },
          { cn: '常用于 POST 后重定向到结果页', en: 'Commonly used to redirect to result page after POST' }
        ],
        example: 'POST 表单后重定向到成功页',
        exampleEn: 'Redirect to success page after POST form'
      },
      { 
        code: 304, 
        name: 'Not Modified', 
        nameCn: '未修改', 
        desc: '资源未修改，可使用缓存版本。',
        descEn: 'The resource has not been modified, cached version can be used.',
        response: '响应体为空，客户端应使用本地缓存。',
        responseEn: 'Response body is empty, client should use local cache.',
        solutions: [
          { cn: '使用本地缓存的资源', en: 'Use locally cached resource' },
          { cn: '检查 ETag 或 Last-Modified 头', en: 'Check ETag or Last-Modified header' },
          { cn: '节省带宽，提高性能', en: 'Save bandwidth, improve performance' }
        ],
        example: '浏览器缓存命中',
        exampleEn: 'Browser cache hit'
      },
      { 
        code: 305, 
        name: 'Use Proxy', 
        nameCn: '使用代理', 
        desc: '请求的资源必须通过指定的代理访问（已废弃）。',
        descEn: 'The requested resource must be accessed through the specified proxy (deprecated).',
        response: '返回代理服务器地址。',
        responseEn: 'Returns proxy server address.',
        solutions: [
          { cn: '此状态码已废弃，不建议使用', en: 'This status code is deprecated, not recommended' },
          { cn: '考虑使用其他方式配置代理', en: 'Consider using other ways to configure proxy' }
        ],
        example: '强制使用代理（已废弃）',
        exampleEn: 'Force proxy usage (deprecated)'
      },
      { 
        code: 307, 
        name: 'Temporary Redirect', 
        nameCn: '临时重定向', 
        desc: '临时重定向，保持原请求方法不变。',
        descEn: 'Temporary redirect, keeping the original request method unchanged.',
        response: '返回 Location 头，客户端应保持原方法重新请求。',
        responseEn: 'Returns Location header, client should re-request with original method.',
        solutions: [
          { cn: '使用相同的请求方法访问新地址', en: 'Use same request method to access new address' },
          { cn: '保留原地址用于后续请求', en: 'Keep original address for subsequent requests' },
          { cn: '比 302 更严格，不会改变方法', en: 'Stricter than 302, will not change method' }
        ],
        example: 'HTTPS 重定向、负载均衡',
        exampleEn: 'HTTPS redirect, load balancing'
      },
      { 
        code: 308, 
        name: 'Permanent Redirect', 
        nameCn: '永久重定向', 
        desc: '永久重定向，保持原请求方法不变。',
        descEn: 'Permanent redirect, keeping the original request method unchanged.',
        response: '返回 Location 头，客户端应永久更新地址并保持原方法。',
        responseEn: 'Returns Location header, client should permanently update address and keep original method.',
        solutions: [
          { cn: '更新所有链接到新地址', en: 'Update all links to new address' },
          { cn: '使用相同方法请求新地址', en: 'Use same method to request new address' },
          { cn: '比 301 更严格，不会改变方法', en: 'Stricter than 301, will not change method' }
        ],
        example: 'API 版本永久迁移',
        exampleEn: 'API version permanent migration'
      }
    ]
  },
  { 
    name: '4xx 客户端错误 (Client Error)', 
    class: 'client-error', 
    codes: [
      { 
        code: 400, 
        name: 'Bad Request', 
        nameCn: '错误请求', 
        desc: '服务器无法理解请求，通常是语法错误。',
        descEn: 'The server cannot understand the request due to invalid syntax.',
        response: '返回错误详情，说明请求哪里有问题。',
        responseEn: 'Returns error details explaining what is wrong with the request.',
        solutions: [
          { cn: '检查请求参数格式是否正确', en: 'Check if request parameter format is correct' },
          { cn: '验证 JSON/XML 语法', en: 'Validate JSON/XML syntax' },
          { cn: '确认 Content-Type 头正确', en: 'Confirm Content-Type header is correct' },
          { cn: '检查必填字段是否缺失', en: 'Check if required fields are missing' }
        ],
        example: '参数格式错误、JSON 解析失败',
        exampleEn: 'Parameter format error, JSON parsing failed'
      },
      { 
        code: 401, 
        name: 'Unauthorized', 
        nameCn: '未授权', 
        desc: '请求需要身份认证。',
        descEn: 'The request requires user authentication.',
        response: '返回 WWW-Authenticate 头，指示认证方式。',
        responseEn: 'Returns WWW-Authenticate header indicating authentication method.',
        solutions: [
          { cn: '检查是否携带有效的认证信息', en: 'Check if valid authentication info is provided' },
          { cn: '刷新过期的 Token', en: 'Refresh expired Token' },
          { cn: '重新登录获取新凭证', en: 'Re-login to get new credentials' },
          { cn: '检查 Authorization 头格式', en: 'Check Authorization header format' }
        ],
        example: '未登录访问需认证接口、Token 过期',
        exampleEn: 'Accessing authenticated API without login, Token expired'
      },
      { 
        code: 402, 
        name: 'Payment Required', 
        nameCn: '需要付款', 
        desc: '保留状态码，用于将来的付费服务。',
        descEn: 'Reserved for future use, intended for digital payment systems.',
        response: '返回付款相关信息。',
        responseEn: 'Returns payment-related information.',
        solutions: [
          { cn: '完成付款流程', en: 'Complete payment process' },
          { cn: '检查账户余额或订阅状态', en: 'Check account balance or subscription status' },
          { cn: '联系服务提供商', en: 'Contact service provider' }
        ],
        example: 'API 调用次数超限需付费',
        exampleEn: 'API call limit exceeded, payment required'
      },
      { 
        code: 403, 
        name: 'Forbidden', 
        nameCn: '禁止访问', 
        desc: '服务器理解请求但拒绝执行。',
        descEn: 'The server understood the request but refuses to authorize it.',
        response: '返回拒绝原因，但不一定详细说明。',
        responseEn: 'Returns rejection reason, but may not be detailed.',
        solutions: [
          { cn: '检查用户权限是否足够', en: 'Check if user has sufficient permissions' },
          { cn: '确认资源访问策略', en: 'Confirm resource access policy' },
          { cn: '联系管理员获取权限', en: 'Contact administrator for permissions' },
          { cn: '检查 IP 是否被封禁', en: 'Check if IP is blocked' }
        ],
        example: '无权限访问管理后台',
        exampleEn: 'No permission to access admin panel'
      },
      { 
        code: 404, 
        name: 'Not Found', 
        nameCn: '未找到', 
        desc: '服务器找不到请求的资源。',
        descEn: 'The server cannot find the requested resource.',
        response: '返回错误页面或错误信息。',
        responseEn: 'Returns error page or error message.',
        solutions: [
          { cn: '检查 URL 是否正确', en: 'Check if URL is correct' },
          { cn: '确认资源是否存在', en: 'Confirm if resource exists' },
          { cn: '检查路由配置', en: 'Check routing configuration' },
          { cn: '查看 API 文档确认端点', en: 'Check API documentation for endpoint' }
        ],
        example: '访问不存在的页面或 API',
        exampleEn: 'Accessing non-existent page or API'
      },
      { 
        code: 405, 
        name: 'Method Not Allowed', 
        nameCn: '方法不允许', 
        desc: '请求方法不被目标资源支持。',
        descEn: 'The request method is not supported by the target resource.',
        response: '返回 Allow 头，列出支持的方法。',
        responseEn: 'Returns Allow header listing supported methods.',
        solutions: [
          { cn: '检查 Allow 头获取支持的方法', en: 'Check Allow header for supported methods' },
          { cn: '使用正确的 HTTP 方法', en: 'Use correct HTTP method' },
          { cn: '查阅 API 文档', en: 'Consult API documentation' }
        ],
        example: '用 GET 访问只支持 POST 的接口',
        exampleEn: 'Using GET on POST-only endpoint'
      },
      { 
        code: 406, 
        name: 'Not Acceptable', 
        nameCn: '不可接受', 
        desc: '服务器无法生成符合 Accept 头要求的响应。',
        descEn: 'The server cannot produce a response matching the Accept headers.',
        response: '返回可用的内容类型列表。',
        responseEn: 'Returns list of available content types.',
        solutions: [
          { cn: '修改 Accept 头为服务器支持的类型', en: 'Modify Accept header to server-supported type' },
          { cn: '检查请求的内容协商头', en: 'Check content negotiation headers' }
        ],
        example: '请求 XML 但服务器只支持 JSON',
        exampleEn: 'Requesting XML but server only supports JSON'
      },
      { 
        code: 407, 
        name: 'Proxy Authentication Required', 
        nameCn: '需要代理认证', 
        desc: '客户端必须先通过代理服务器认证。',
        descEn: 'The client must first authenticate itself with the proxy.',
        response: '返回 Proxy-Authenticate 头。',
        responseEn: 'Returns Proxy-Authenticate header.',
        solutions: [
          { cn: '提供代理服务器认证信息', en: 'Provide proxy server authentication' },
          { cn: '配置代理认证凭证', en: 'Configure proxy authentication credentials' }
        ],
        example: '企业代理需要认证',
        exampleEn: 'Corporate proxy requires authentication'
      },
      { 
        code: 408, 
        name: 'Request Timeout', 
        nameCn: '请求超时', 
        desc: '服务器等待请求超时。',
        descEn: 'The server timed out waiting for the request.',
        response: '连接可能被关闭。',
        responseEn: 'Connection may be closed.',
        solutions: [
          { cn: '检查网络连接', en: 'Check network connection' },
          { cn: '重新发送请求', en: 'Resend the request' },
          { cn: '减少请求数据量', en: 'Reduce request data size' },
          { cn: '增加客户端超时设置', en: 'Increase client timeout setting' }
        ],
        example: '网络慢导致请求未能及时完成',
        exampleEn: 'Slow network causing request not completed in time'
      },
      { 
        code: 409, 
        name: 'Conflict', 
        nameCn: '冲突', 
        desc: '请求与目标资源的当前状态冲突。',
        descEn: 'The request conflicts with the current state of the target resource.',
        response: '返回冲突详情和当前资源状态。',
        responseEn: 'Returns conflict details and current resource state.',
        solutions: [
          { cn: '获取最新资源状态后重试', en: 'Get latest resource state and retry' },
          { cn: '解决版本冲突', en: 'Resolve version conflict' },
          { cn: '使用乐观锁机制', en: 'Use optimistic locking mechanism' }
        ],
        example: '创建已存在的资源、并发编辑冲突',
        exampleEn: 'Creating existing resource, concurrent edit conflict'
      },
      { 
        code: 410, 
        name: 'Gone', 
        nameCn: '已删除', 
        desc: '资源已被永久删除，不再可用。',
        descEn: 'The resource is no longer available and will not be available again.',
        response: '返回空响应或删除说明。',
        responseEn: 'Returns empty response or deletion explanation.',
        solutions: [
          { cn: '移除对该资源的引用', en: 'Remove references to this resource' },
          { cn: '更新缓存和书签', en: 'Update cache and bookmarks' },
          { cn: '与 404 不同，410 表示永久删除', en: 'Unlike 404, 410 indicates permanent deletion' }
        ],
        example: '已下架的商品、已删除的文章',
        exampleEn: 'Discontinued product, deleted article'
      },
      { 
        code: 411, 
        name: 'Length Required', 
        nameCn: '需要内容长度', 
        desc: '服务器要求请求必须包含 Content-Length 头。',
        descEn: 'The server requires the Content-Length header in the request.',
        response: '返回错误信息。',
        responseEn: 'Returns error message.',
        solutions: [
          { cn: '添加 Content-Length 头', en: 'Add Content-Length header' },
          { cn: '确保请求体长度正确', en: 'Ensure request body length is correct' }
        ],
        example: '上传文件未指定长度',
        exampleEn: 'File upload without specifying length'
      },
      { 
        code: 412, 
        name: 'Precondition Failed', 
        nameCn: '前提条件失败', 
        desc: '请求头中的前提条件不满足。',
        descEn: 'One or more conditions in the request header fields evaluated to false.',
        response: '返回当前资源状态。',
        responseEn: 'Returns current resource state.',
        solutions: [
          { cn: '获取最新的 ETag 或 Last-Modified', en: 'Get latest ETag or Last-Modified' },
          { cn: '更新条件头后重试', en: 'Update conditional headers and retry' }
        ],
        example: 'If-Match 条件不满足',
        exampleEn: 'If-Match condition not met'
      },
      { 
        code: 413, 
        name: 'Payload Too Large', 
        nameCn: '请求体过大', 
        desc: '请求体超过服务器处理能力。',
        descEn: 'The request payload is larger than the server is willing to process.',
        response: '可能返回 Retry-After 头。',
        responseEn: 'May return Retry-After header.',
        solutions: [
          { cn: '压缩请求数据', en: 'Compress request data' },
          { cn: '分块上传大文件', en: 'Upload large files in chunks' },
          { cn: '调整服务器上传限制', en: 'Adjust server upload limit' }
        ],
        example: '上传文件超过大小限制',
        exampleEn: 'Uploaded file exceeds size limit'
      },
      { 
        code: 414, 
        name: 'URI Too Long', 
        nameCn: 'URI 过长', 
        desc: '请求的 URI 超过服务器处理能力。',
        descEn: 'The URI requested by the client is longer than the server is willing to interpret.',
        response: '返回错误信息。',
        responseEn: 'Returns error message.',
        solutions: [
          { cn: '使用 POST 代替 GET 传递大量参数', en: 'Use POST instead of GET for large parameters' },
          { cn: '缩短查询字符串', en: 'Shorten query string' }
        ],
        example: 'GET 请求参数过多',
        exampleEn: 'GET request with too many parameters'
      },
      { 
        code: 415, 
        name: 'Unsupported Media Type', 
        nameCn: '不支持的媒体类型', 
        desc: '服务器不支持请求的媒体格式。',
        descEn: 'The server does not support the media format of the requested data.',
        response: '返回支持的媒体类型列表。',
        responseEn: 'Returns list of supported media types.',
        solutions: [
          { cn: '检查 Content-Type 头是否正确', en: 'Check if Content-Type header is correct' },
          { cn: '转换数据格式', en: 'Convert data format' },
          { cn: '查阅 API 支持的格式', en: 'Check API supported formats' }
        ],
        example: '发送 XML 但 API 只接受 JSON',
        exampleEn: 'Sending XML but API only accepts JSON'
      },
      { 
        code: 416, 
        name: 'Range Not Satisfiable', 
        nameCn: '范围不满足', 
        desc: '请求的 Range 头超出资源范围。',
        descEn: 'The range specified in the Range header cannot be fulfilled.',
        response: '返回 Content-Range 头显示实际范围。',
        responseEn: 'Returns Content-Range header showing actual range.',
        solutions: [
          { cn: '检查请求的范围是否有效', en: 'Check if requested range is valid' },
          { cn: '获取资源实际大小后重试', en: 'Get actual resource size and retry' }
        ],
        example: '断点续传时请求超出文件大小',
        exampleEn: 'Resume download requesting beyond file size'
      },
      { 
        code: 417, 
        name: 'Expectation Failed', 
        nameCn: '期望失败', 
        desc: '服务器无法满足 Expect 请求头的要求。',
        descEn: 'The server cannot meet the requirements of the Expect request header.',
        response: '返回错误信息。',
        responseEn: 'Returns error message.',
        solutions: [
          { cn: '移除 Expect 头后重试', en: 'Remove Expect header and retry' },
          { cn: '检查服务器是否支持该期望', en: 'Check if server supports the expectation' }
        ],
        example: 'Expect: 100-continue 不被支持',
        exampleEn: 'Expect: 100-continue not supported'
      },
      { 
        code: 418, 
        name: "I'm a teapot", 
        nameCn: '我是茶壶', 
        desc: '服务器拒绝冲泡咖啡，因为它是茶壶（愚人节玩笑）。',
        descEn: "The server refuses to brew coffee because it is a teapot (April Fools' joke).",
        response: '返回幽默的错误信息。',
        responseEn: 'Returns humorous error message.',
        solutions: [
          { cn: '这是一个彩蛋状态码', en: 'This is an Easter egg status code' },
          { cn: '来自 RFC 2324 超文本咖啡壶控制协议', en: 'From RFC 2324 Hyper Text Coffee Pot Control Protocol' }
        ],
        example: '开发者彩蛋',
        exampleEn: 'Developer Easter egg'
      },
      { 
        code: 421, 
        name: 'Misdirected Request', 
        nameCn: '请求被误导', 
        desc: '请求被定向到无法生成响应的服务器。',
        descEn: 'The request was directed at a server that is not able to produce a response.',
        response: '返回错误信息。',
        responseEn: 'Returns error message.',
        solutions: [
          { cn: '检查请求的目标服务器', en: 'Check target server of request' },
          { cn: '可能是 HTTP/2 连接复用问题', en: 'May be HTTP/2 connection reuse issue' }
        ],
        example: 'HTTP/2 连接复用到错误的服务器',
        exampleEn: 'HTTP/2 connection reused to wrong server'
      },
      { 
        code: 422, 
        name: 'Unprocessable Entity', 
        nameCn: '无法处理的实体', 
        desc: '请求格式正确但语义错误，无法处理。',
        descEn: 'The request was well-formed but semantically incorrect.',
        response: '返回验证错误详情。',
        responseEn: 'Returns validation error details.',
        solutions: [
          { cn: '检查业务逻辑验证规则', en: 'Check business logic validation rules' },
          { cn: '修正数据内容而非格式', en: 'Fix data content, not format' },
          { cn: '查看返回的具体错误字段', en: 'Check specific error fields in response' }
        ],
        example: '表单验证失败、业务规则不满足',
        exampleEn: 'Form validation failed, business rules not met'
      },
      { 
        code: 423, 
        name: 'Locked', 
        nameCn: '已锁定', 
        desc: '资源被锁定，无法访问。',
        descEn: 'The resource is locked and cannot be accessed.',
        response: '返回锁定信息。',
        responseEn: 'Returns lock information.',
        solutions: [
          { cn: '等待资源解锁', en: 'Wait for resource to be unlocked' },
          { cn: '联系资源所有者', en: 'Contact resource owner' },
          { cn: '检查是否有其他进程占用', en: 'Check if other processes are using it' }
        ],
        example: 'WebDAV 文件被其他用户锁定',
        exampleEn: 'WebDAV file locked by another user'
      },
      { 
        code: 424, 
        name: 'Failed Dependency', 
        nameCn: '依赖失败', 
        desc: '由于之前的请求失败，当前请求也失败。',
        descEn: 'The request failed due to failure of a previous request.',
        response: '返回依赖失败的详情。',
        responseEn: 'Returns details of dependency failure.',
        solutions: [
          { cn: '检查并修复依赖的请求', en: 'Check and fix dependent request' },
          { cn: '按正确顺序重新执行请求', en: 'Re-execute requests in correct order' }
        ],
        example: 'WebDAV 批量操作中某个操作失败',
        exampleEn: 'One operation failed in WebDAV batch operation'
      },
      { 
        code: 425, 
        name: 'Too Early', 
        nameCn: '过早', 
        desc: '服务器不愿意处理可能被重放的请求。',
        descEn: 'The server is unwilling to process a request that might be replayed.',
        response: '返回错误信息。',
        responseEn: 'Returns error message.',
        solutions: [
          { cn: '等待 TLS 握手完成后重试', en: 'Wait for TLS handshake to complete and retry' },
          { cn: '用于防止 TLS 1.3 早期数据重放攻击', en: 'Used to prevent TLS 1.3 early data replay attacks' }
        ],
        example: 'TLS 1.3 早期数据请求',
        exampleEn: 'TLS 1.3 early data request'
      },
      { 
        code: 426, 
        name: 'Upgrade Required', 
        nameCn: '需要升级', 
        desc: '客户端应切换到更高版本的协议。',
        descEn: 'The client should switch to a different protocol.',
        response: '返回 Upgrade 头指示需要的协议。',
        responseEn: 'Returns Upgrade header indicating required protocol.',
        solutions: [
          { cn: '升级到指定的协议版本', en: 'Upgrade to specified protocol version' },
          { cn: '检查 Upgrade 头获取要求', en: 'Check Upgrade header for requirements' }
        ],
        example: '需要升级到 HTTPS 或 HTTP/2',
        exampleEn: 'Need to upgrade to HTTPS or HTTP/2'
      },
      { 
        code: 428, 
        name: 'Precondition Required', 
        nameCn: '需要前提条件', 
        desc: '服务器要求请求必须是条件请求。',
        descEn: 'The server requires the request to be conditional.',
        response: '返回错误信息。',
        responseEn: 'Returns error message.',
        solutions: [
          { cn: '添加 If-Match 或 If-Unmodified-Since 头', en: 'Add If-Match or If-Unmodified-Since header' },
          { cn: '用于防止丢失更新问题', en: 'Used to prevent lost update problem' }
        ],
        example: '更新资源需要提供 ETag',
        exampleEn: 'Resource update requires ETag'
      },
      { 
        code: 429, 
        name: 'Too Many Requests', 
        nameCn: '请求过多', 
        desc: '用户在给定时间内发送了太多请求。',
        descEn: 'The user has sent too many requests in a given amount of time.',
        response: '返回 Retry-After 头指示等待时间。',
        responseEn: 'Returns Retry-After header indicating wait time.',
        solutions: [
          { cn: '等待 Retry-After 指定的时间后重试', en: 'Wait for time specified in Retry-After and retry' },
          { cn: '实现请求限流和退避策略', en: 'Implement request throttling and backoff strategy' },
          { cn: '检查是否有请求泄漏', en: 'Check for request leaks' },
          { cn: '考虑升级 API 配额', en: 'Consider upgrading API quota' }
        ],
        example: 'API 调用频率超限',
        exampleEn: 'API call rate limit exceeded'
      },
      { 
        code: 431, 
        name: 'Request Header Fields Too Large', 
        nameCn: '请求头字段过大', 
        desc: '请求头字段太大，服务器拒绝处理。',
        descEn: 'The server refuses to process the request because header fields are too large.',
        response: '返回错误信息。',
        responseEn: 'Returns error message.',
        solutions: [
          { cn: '减少请求头大小', en: 'Reduce request header size' },
          { cn: '清理不必要的 Cookie', en: 'Clean up unnecessary cookies' },
          { cn: '检查是否有过大的自定义头', en: 'Check for oversized custom headers' }
        ],
        example: 'Cookie 过多导致请求头过大',
        exampleEn: 'Too many cookies causing oversized headers'
      },
      { 
        code: 451, 
        name: 'Unavailable For Legal Reasons', 
        nameCn: '因法律原因不可用', 
        desc: '由于法律原因，资源不可用。',
        descEn: 'The resource is unavailable due to legal reasons.',
        response: '返回法律原因说明。',
        responseEn: 'Returns explanation of legal reasons.',
        solutions: [
          { cn: '了解具体的法律限制', en: 'Understand specific legal restrictions' },
          { cn: '可能需要从其他地区访问', en: 'May need to access from another region' },
          { cn: '状态码来自《华氏451度》', en: 'Status code from "Fahrenheit 451"' }
        ],
        example: '因版权或审查原因被屏蔽',
        exampleEn: 'Blocked due to copyright or censorship'
      }
    ]
  },
  { 
    name: '5xx 服务器错误 (Server Error)', 
    class: 'server-error', 
    codes: [
      { 
        code: 500, 
        name: 'Internal Server Error', 
        nameCn: '服务器内部错误', 
        desc: '服务器遇到意外情况，无法完成请求。',
        descEn: 'The server encountered an unexpected condition that prevented it from fulfilling the request.',
        response: '返回通用错误信息，详细错误通常记录在服务器日志。',
        responseEn: 'Returns generic error message, detailed errors usually logged on server.',
        solutions: [
          { cn: '检查服务器错误日志', en: 'Check server error logs' },
          { cn: '检查代码是否有未捕获的异常', en: 'Check for uncaught exceptions in code' },
          { cn: '验证数据库连接和查询', en: 'Verify database connection and queries' },
          { cn: '检查第三方服务依赖', en: 'Check third-party service dependencies' },
          { cn: '稍后重试请求', en: 'Retry request later' }
        ],
        example: '代码异常未捕获、数据库查询错误',
        exampleEn: 'Uncaught code exception, database query error'
      },
      { 
        code: 501, 
        name: 'Not Implemented', 
        nameCn: '未实现', 
        desc: '服务器不支持请求的功能。',
        descEn: 'The server does not support the functionality required to fulfill the request.',
        response: '返回错误信息说明不支持的功能。',
        responseEn: 'Returns error message explaining unsupported functionality.',
        solutions: [
          { cn: '检查请求方法是否被服务器支持', en: 'Check if request method is supported by server' },
          { cn: '查阅 API 文档了解支持的功能', en: 'Consult API documentation for supported features' },
          { cn: '使用替代方法实现需求', en: 'Use alternative methods to achieve requirement' }
        ],
        example: '服务器不支持 TRACE 方法',
        exampleEn: 'Server does not support TRACE method'
      },
      { 
        code: 502, 
        name: 'Bad Gateway', 
        nameCn: '网关错误', 
        desc: '网关或代理服务器从上游服务器收到无效响应。',
        descEn: 'The gateway or proxy server received an invalid response from the upstream server.',
        response: '返回网关错误信息。',
        responseEn: 'Returns gateway error message.',
        solutions: [
          { cn: '检查上游服务器是否正常运行', en: 'Check if upstream server is running normally' },
          { cn: '检查网络连接和防火墙设置', en: 'Check network connection and firewall settings' },
          { cn: '查看代理服务器配置', en: 'Check proxy server configuration' },
          { cn: '稍后重试请求', en: 'Retry request later' }
        ],
        example: 'Nginx 后端服务不可用',
        exampleEn: 'Nginx backend service unavailable'
      },
      { 
        code: 503, 
        name: 'Service Unavailable', 
        nameCn: '服务不可用', 
        desc: '服务器暂时无法处理请求，通常是过载或维护。',
        descEn: 'The server is temporarily unable to handle the request, usually due to overload or maintenance.',
        response: '可能返回 Retry-After 头指示恢复时间。',
        responseEn: 'May return Retry-After header indicating recovery time.',
        solutions: [
          { cn: '等待 Retry-After 指定的时间', en: 'Wait for time specified in Retry-After' },
          { cn: '检查服务器负载和资源使用', en: 'Check server load and resource usage' },
          { cn: '扩展服务器容量', en: 'Scale server capacity' },
          { cn: '检查是否在维护窗口期', en: 'Check if in maintenance window' }
        ],
        example: '服务器维护中、流量过大',
        exampleEn: 'Server under maintenance, traffic overload'
      },
      { 
        code: 504, 
        name: 'Gateway Timeout', 
        nameCn: '网关超时', 
        desc: '网关或代理服务器未能及时从上游服务器获得响应。',
        descEn: 'The gateway or proxy server did not receive a timely response from the upstream server.',
        response: '返回超时错误信息。',
        responseEn: 'Returns timeout error message.',
        solutions: [
          { cn: '检查上游服务器响应时间', en: 'Check upstream server response time' },
          { cn: '增加网关超时设置', en: 'Increase gateway timeout setting' },
          { cn: '优化上游服务性能', en: 'Optimize upstream service performance' },
          { cn: '检查网络延迟', en: 'Check network latency' }
        ],
        example: '后端 API 响应超时',
        exampleEn: 'Backend API response timeout'
      },
      { 
        code: 505, 
        name: 'HTTP Version Not Supported', 
        nameCn: 'HTTP 版本不支持', 
        desc: '服务器不支持请求使用的 HTTP 版本。',
        descEn: 'The server does not support the HTTP version used in the request.',
        response: '返回支持的 HTTP 版本信息。',
        responseEn: 'Returns supported HTTP version information.',
        solutions: [
          { cn: '使用服务器支持的 HTTP 版本', en: 'Use HTTP version supported by server' },
          { cn: '升级服务器以支持新版本', en: 'Upgrade server to support new version' }
        ],
        example: '使用 HTTP/2 但服务器只支持 HTTP/1.1',
        exampleEn: 'Using HTTP/2 but server only supports HTTP/1.1'
      },
      { 
        code: 506, 
        name: 'Variant Also Negotiates', 
        nameCn: '变体协商', 
        desc: '服务器存在内部配置错误，透明内容协商导致循环引用。',
        descEn: 'The server has an internal configuration error: transparent content negotiation results in a circular reference.',
        response: '返回配置错误信息。',
        responseEn: 'Returns configuration error message.',
        solutions: [
          { cn: '检查服务器内容协商配置', en: 'Check server content negotiation configuration' },
          { cn: '修复循环引用问题', en: 'Fix circular reference issue' }
        ],
        example: '内容协商配置错误',
        exampleEn: 'Content negotiation configuration error'
      },
      { 
        code: 507, 
        name: 'Insufficient Storage', 
        nameCn: '存储空间不足', 
        desc: '服务器无法存储完成请求所需的内容。',
        descEn: 'The server is unable to store the representation needed to complete the request.',
        response: '返回存储错误信息。',
        responseEn: 'Returns storage error message.',
        solutions: [
          { cn: '清理服务器存储空间', en: 'Clean up server storage space' },
          { cn: '扩展存储容量', en: 'Expand storage capacity' },
          { cn: '检查磁盘配额设置', en: 'Check disk quota settings' }
        ],
        example: 'WebDAV 存储空间已满',
        exampleEn: 'WebDAV storage space full'
      },
      { 
        code: 508, 
        name: 'Loop Detected', 
        nameCn: '检测到循环', 
        desc: '服务器在处理请求时检测到无限循环。',
        descEn: 'The server detected an infinite loop while processing the request.',
        response: '返回循环检测错误信息。',
        responseEn: 'Returns loop detection error message.',
        solutions: [
          { cn: '检查资源绑定是否存在循环', en: 'Check if resource bindings have loops' },
          { cn: '修复 WebDAV 绑定配置', en: 'Fix WebDAV binding configuration' }
        ],
        example: 'WebDAV 绑定形成循环',
        exampleEn: 'WebDAV bindings form a loop'
      },
      { 
        code: 510, 
        name: 'Not Extended', 
        nameCn: '未扩展', 
        desc: '服务器需要对请求进行进一步扩展才能完成。',
        descEn: 'Further extensions to the request are required for the server to fulfill it.',
        response: '返回所需扩展的信息。',
        responseEn: 'Returns information about required extensions.',
        solutions: [
          { cn: '添加服务器要求的扩展', en: 'Add extensions required by server' },
          { cn: '查阅文档了解所需扩展', en: 'Consult documentation for required extensions' }
        ],
        example: 'HTTP 扩展框架请求',
        exampleEn: 'HTTP Extension Framework request'
      },
      { 
        code: 511, 
        name: 'Network Authentication Required', 
        nameCn: '需要网络认证', 
        desc: '客户端需要进行网络认证才能访问。',
        descEn: 'The client needs to authenticate to gain network access.',
        response: '返回认证页面或认证要求。',
        responseEn: 'Returns authentication page or authentication requirements.',
        solutions: [
          { cn: '完成网络认证（如 WiFi 登录）', en: 'Complete network authentication (e.g., WiFi login)' },
          { cn: '检查是否在强制门户网络中', en: 'Check if in captive portal network' },
          { cn: '联系网络管理员', en: 'Contact network administrator' }
        ],
        example: '公共 WiFi 需要登录认证',
        exampleEn: 'Public WiFi requires login authentication'
      }
    ]
  }
]

const filteredGroups = computed(() => {
  if (!search.value) return statusGroups
  const s = search.value.toLowerCase()
  return statusGroups.map(g => ({
    ...g,
    codes: g.codes.filter(c => 
      String(c.code).includes(s) || 
      c.name.toLowerCase().includes(s) || 
      c.nameCn.includes(s) ||
      c.desc.includes(s) ||
      c.descEn.toLowerCase().includes(s)
    )
  })).filter(g => g.codes.length > 0)
})
</script>

<style scoped>
.tool-content { 
  display: flex; 
  flex-direction: column; 
  gap: 1rem; 
}

.search-box input { 
  width: 100%; 
  padding: 0.75rem 1rem; 
  border: 1px solid #ddd; 
  border-radius: 8px; 
  font-size: 14px;
}

.search-box input:focus { 
  outline: none; 
  border-color: #667eea; 
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.status-list { 
  display: flex; 
  flex-direction: column; 
  gap: 1rem; 
  max-height: 350px; 
  overflow-y: auto; 
}

.status-group h4 { 
  margin: 0 0 0.5rem; 
  padding: 0.5rem 0.75rem; 
  border-radius: 6px; 
  font-size: 0.9rem; 
  font-weight: 600;
}

.status-group h4.info { background: #e0f2fe; color: #0369a1; }
.status-group h4.success { background: #dcfce7; color: #166534; }
.status-group h4.redirect { background: #fef3c7; color: #92400e; }
.status-group h4.client-error { background: #fee2e2; color: #991b1b; }
.status-group h4.server-error { background: #fce7f3; color: #9d174d; }

.status-item { 
  display: flex; 
  align-items: center; 
  gap: 1rem; 
  padding: 0.5rem 0.75rem; 
  cursor: pointer; 
  border-radius: 6px; 
  transition: background 0.2s;
}

.status-item:hover { 
  background: #f1f5f9; 
}

.code { 
  font-weight: bold; 
  font-family: 'Monaco', 'Menlo', monospace; 
  min-width: 45px;
  font-size: 14px;
}

.code.info { color: #0369a1; }
.code.success { color: #166534; }
.code.redirect { color: #92400e; }
.code.client-error { color: #991b1b; }
.code.server-error { color: #9d174d; }

.name { 
  color: #334155; 
  font-family: 'Monaco', 'Menlo', monospace; 
  font-size: 13px;
}

.name-cn { 
  color: #64748b; 
  font-size: 13px; 
  margin-left: auto; 
}

/* 详情面板样式 */
.detail-panel { 
  padding: 1.25rem; 
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px; 
  border: 1px solid #e2e8f0;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.25rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e2e8f0;
}

.detail-code {
  font-size: 2rem;
  font-weight: 700;
  font-family: 'Monaco', 'Menlo', monospace;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  background: white;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

.detail-code.info { color: #0369a1; border: 2px solid #0369a1; }
.detail-code.success { color: #166534; border: 2px solid #166534; }
.detail-code.redirect { color: #92400e; border: 2px solid #92400e; }
.detail-code.client-error { color: #991b1b; border: 2px solid #991b1b; }
.detail-code.server-error { color: #9d174d; border: 2px solid #9d174d; }

.detail-titles h3 {
  margin: 0;
  font-size: 1.25rem;
  color: #1e293b;
  font-weight: 600;
}

.cn-title { 
  color: #64748b; 
  font-size: 1rem; 
}

.detail-section {
  margin-bottom: 1rem;
  padding: 1rem;
  background: white;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
}

.detail-section:last-child {
  margin-bottom: 0;
}

.section-title {
  font-weight: 600;
  color: #475569;
  margin-bottom: 0.75rem;
  font-size: 0.9rem;
}

.desc-text,
.response-text,
.example-text {
  margin: 0 0 0.5rem;
  color: #334155;
  line-height: 1.6;
}

.desc-text-en,
.response-text-en,
.example-text-en {
  margin: 0;
  color: #64748b;
  font-size: 0.9rem;
  font-style: italic;
  line-height: 1.5;
}

.solution-list {
  margin: 0;
  padding: 0;
  list-style: none;
}

.solution-list li {
  padding: 0.5rem 0;
  border-bottom: 1px dashed #e2e8f0;
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.solution-list li:last-child {
  border-bottom: none;
}

.solution-cn {
  color: #334155;
  position: relative;
  padding-left: 1.25rem;
}

.solution-cn::before {
  content: '✓';
  position: absolute;
  left: 0;
  color: #22c55e;
  font-weight: bold;
}

.solution-en {
  color: #64748b;
  font-size: 0.85rem;
  font-style: italic;
  padding-left: 1.25rem;
}

/* 滚动条样式 */
.status-list::-webkit-scrollbar {
  width: 6px;
}

.status-list::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 3px;
}

.status-list::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 3px;
}

.status-list::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}
</style>
