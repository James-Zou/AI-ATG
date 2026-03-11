/*
 * Copyright 2026 James Zou
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aiatg;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * AI-ATG 自动化测试平台主应用类
 */
@SpringBootApplication
@EnableAsync
@EnableCaching
@MapperScan("com.aiatg.mapper")
public class AiAtgApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiAtgApplication.class, args);
        System.out.println("AI-ATG Backend Started Successfully!");
    }
}
