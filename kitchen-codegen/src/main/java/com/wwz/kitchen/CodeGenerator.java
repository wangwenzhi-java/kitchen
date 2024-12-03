package com.wwz.kitchen;


import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.rules.DateType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Created by wenzhi.wang.
 * on 2024/11/14.
 */
public class CodeGenerator {
    private static final List<String> tables = Arrays.asList();

    public static void main(String[] args) {
        //批量完成
        tables.forEach(x -> startGenCode(x));
    }


    private static void startGenCode(String tableName) {
        // 1. 创建代码生成器
        AutoGenerator generator = new AutoGenerator();
        // 2. 配置全局配置
        GlobalConfig globalConfig = new GlobalConfig();
        globalConfig.setOutputDir(System.getProperty("user.dir") + File.separator + "kitchen-codegen" + File.separator + "src"); // 代码生成路径
        globalConfig.setAuthor("wenzhi.wang"); // 作者
        globalConfig.setOpen(false); // 生成后是否打开文件夹
        globalConfig.setFileOverride(true); // 是否覆盖已有文件
        globalConfig.setServiceName("%sService"); // 去掉 Service 接口的前缀 "I"
        globalConfig.setDateType(DateType.ONLY_DATE); // 配置时间类型策略（date类型），如果不配置会生成LocalDate类型
        // 设置自定义类型转换器
        generator.setGlobalConfig(globalConfig);

        // 3. 配置数据源
        DataSourceConfig dataSourceConfig = new DataSourceConfig();
        dataSourceConfig.setUrl("jdbc:mysql://localhost:3306/kitchen?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&autoReconnect=true&zeroDateTimeBehavior=convertToNull&allowMultiQueries=true&useSSL=false&allowPublicKeyRetrieval=true&useLegacyDatetimeCode=false");
        dataSourceConfig.setDriverName("com.mysql.cj.jdbc.Driver");
        dataSourceConfig.setUsername("root");
        dataSourceConfig.setPassword("root");
        generator.setDataSource(dataSourceConfig);

        // 4. 配置包名
        PackageConfig packageConfig = new PackageConfig();
        packageConfig.setParent("com.wwz.kitchen"); // 包名
        packageConfig.setModuleName("generator"); // 模块名
        packageConfig.setEntity("entity"); // 实体类包名
        packageConfig.setMapper("mapper"); // Mapper包名
        packageConfig.setService("service"); // Service包名
        packageConfig.setXml("mapper.xml"); // Mapper XML包名
        generator.setPackageInfo(packageConfig);

        // 5. 配置策略
        StrategyConfig strategyConfig = new StrategyConfig();
        strategyConfig.setNaming(NamingStrategy.underline_to_camel); // 数据库表名映射到实体的命名策略（例如：user_info => userInfo）
        strategyConfig.setColumnNaming(NamingStrategy.underline_to_camel); // 字段名映射为驼峰命名
        strategyConfig.setEntityLombokModel(true); // 是否使用 Lombok
        strategyConfig.setRestControllerStyle(false); // 生成 @RestController 控制器
        strategyConfig.setInclude(tableName); // 需要生成的表名
        generator.setStrategy(strategyConfig);
        strategyConfig.setEntityTableFieldAnnotationEnable(true); // 启用表字段注解
/*
        TemplateConfig templateConfig = new TemplateConfig();
        templateConfig.setXml("/mapper.xml.vm");  // 自定义模板文件
        generator.setTemplate(templateConfig);
*/
        // 6. 执行生成
        generator.execute();
    }
}


