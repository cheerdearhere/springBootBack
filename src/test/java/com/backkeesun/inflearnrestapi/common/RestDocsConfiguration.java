package com.backkeesun.inflearnrestapi.common;

import org.springframework.boot.test.autoconfigure.restdocs.RestDocsMockMvcConfigurationCustomizer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;

@TestConfiguration // test 관련 설정임을 알림
public class RestDocsConfiguration {
    @Bean
    public RestDocsMockMvcConfigurationCustomizer restDocsMockMvcConfigurationCustomizer(){
        //람다식 표현
        return configurer -> configurer.operationPreprocessors() // 처리과정 정의
                .withResponseDefaults(prettyPrint()) //response 결과를 보기 좋게
                .withRequestDefaults(prettyPrint()); //request 표시를 보기 좋게

        //내부 익명클래스
        //        return new RestDocsMockMvcConfigurationCustomizer() {
//            @Override
//            public void customize(MockMvcRestDocumentationConfigurer configurer) {
//                //여기까지는 자동생성. configurer만 정의
//                configurer.operationPreprocessors() // 처리과정 정의
//                        .withResponseDefaults(prettyPrint()) //response 결과를 보기 좋게
//                        .withRequestDefaults(prettyPrint()); //request 표시를 보기 좋게
//            }
//        };
    }
}
