/*
 * @Author: 18582297328 2622013323@qq.com
 * @Date: 2025-08-05 09:47:11
 * @LastEditors: 18582297328 2622013323@qq.com
 * @LastEditTime: 2025-12-03 10:46:04
 * @FilePath: \ai博客\blog\backend-spring\src\main\java\com\blog\BackendApplication.java
 * @Description: 这是默认设置,请设置`customMade`, 打开koroFileHeader查看配置 进行设置: https://github.com/OBKoro1/koro1FileHeader/wiki/%E9%85%8D%E7%BD%AE
 */
package com.blog;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.blog.mapper")
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}