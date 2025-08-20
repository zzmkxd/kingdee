package plugins.test;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextToJsonConverter {

    public static void main(String[] args) {
        String inputText = "1 [初识MyBatis框架]\n" +
                "1.1 [框架概述](框架的定义、在软件开发中的作用及其优势)\n" +
                "1.1.1 [框架的优势](提高开发效率、代码规范性、软件性能和降低维护成本)\n" +
                "1.1.2 [当前主流框架](Spring框架、Spring MVC框架、MyBatis框架及Spring Boot框架)\n" +
                "1.1.3 [框架的必要性](解决代码冗余、提高维护性和可扩展性)\n" +
                "\n" +
                "1.2 [MyBatis介绍](MyBatis框架的基本概念和特点)\n" +
                "1.2.1 [传统JDBC的劣势](连接创建开销大、SQL硬编码、参数设置复杂和结果集处理繁琐)\n" +
                "1.2.2 [MyBatis概述](ORM框架，简化数据库操作，自动映射对象和数据表)\n" +
                "\n" +
                "1.3 [MyBatis环境搭建](搭建MyBatis开发环境的基本步骤)\n" +
                "1.3.1 [创建工程](使用IDEA创建Maven项目结构)\n" +
                "1.3.2 [引入依赖](添加MyBatis、MySQL驱动和JUnit依赖)\n" +
                "1.3.3 [配置文件](创建数据库连接信息配置文件)\n" +
                "\n" +
                "1.4 [MyBatis入门程序](实现基本的数据库查询操作)\n" +
                "1.4.1 [核心配置文件](介绍SqlSessionFactory配置相关的配置项)\n" +
                "1.4.2 [映射文件](在映射文件中定义SQL语句和映射关系)\n" +
                "1.4.3 [Simple查询](编写简单的查询示例程序)\n" +
                "\n" +
                "2 [MyBatis的核心配置]\n" +
                "2.1 [MyBatis核心对象](核心对象的功能及作用)\n" +
                "2.1.1 [SqlSessionFactoryBuilder](用于构建SqlSessionFactory的类)\n" +
                "2.1.2 [SqlSessionFactory](用于创建SqlSession实例的工厂)\n" +
                "2.1.3 [SqlSession](用于执行数据库操作的对象)\n" +
                "\n" +
                "2.2 [MyBatis核心配置文件](配置文件结构及各元素的作用)\n" +
                "2.2.1 [配置文件主要元素](介绍配置文件中各个主要标签的使用)\n" +
                "2.2.2 [<properties>元素](配置属性文件和属性值)\n" +
                "2.2.3 [<settings>元素](设置MyBatis运行时的行为参数)\n" +
                "\n" +
                "2.3 [MyBatis映射文件](映射文件的常用元素及使用方法)\n" +
                "2.3.1 [常用元素介绍](介绍映射文件中的各种元素标签)\n" +
                "2.3.2 [<select>元素](定义查询语句和结果映射)\n" +
                "2.3.3 [<insert>元素](定义插入语句和参数处理)\n" +
                "\n" +
                "3 [动态SQL]\n" +
                "3.1 [动态SQL元素](常用动态SQL标签的概述)\n" +
                "3.1.1 [<if>元素](条件判断标签，根据条件动态生成SQL)\n" +
                "3.1.2 [<choose>, <when>, <otherwise>元素](多条件分支判断结构)\n" +
                "3.1.3 [<where>, <trim>元素](处理WHERE子句和SQL片段处理)\n" +
                "\n" +
                "3.2 [条件查询操作](动态SQL在查询中的应用)\n" +
                "3.2.1 [<if>元素的使用](结合条件表达式动态构建查询语句)\n" +
                "3.2.2 [<choose>元素的使用](多条件选择执行不同语句)\n" +
                "3.2.3 [<where>元素的使用](自动处理WHERE关键字及条件拼接)\n" +
                "\n" +
                "3.3 [更新操作](动态SQL在更新中的应用)\n" +
                "3.3.1 [更新语句构建](使用动态标签生成更新SQL)\n" +
                "3.3.2 [条件更新设置](根据条件动态更新特定字段)\n" +
                "3.3.3 [参数绑定处理](处理动态参数传递)\n" +
                "\n" +
                "4 [MyBatis的关联映射和缓存机制]\n" +
                "4.1 [关联映射概述](关联关系的处理方法和映射机制)\n" +
                "4.1.1 [一对一关联映射](处理实体间一对一关系的映射)\n" +
                "4.1.2 [一对多关联映射](处理实体间一对多关系的映射)\n" +
                "4.1.3 [多对多关联映射](处理实体间多对多关系的映射)\n" +
                "\n" +
                "4.2 [MyBatis缓存机制](一级缓存与二级缓存的机制和应用)\n" +
                "4.2.1 [一级缓存](本地缓存特点和缓存生命周期)\n" +
                "4.2.2 [二级缓存](全局缓存的配置和使用方式)\n" +
                "4.2.3 [缓存策略](缓存失效机制和缓存配置选项)\n" +
                "\n" +
                "5 [MyBatis的注解开发]\n" +
                "5.1 [基于注解的单表增删改查](使用注解实现基本数据库操作)\n" +
                "5.1.1 [@Select注解](用于定义查询语句的注解)\n" +
                "5.1.2 [@Insert注解](用于定义插入语句的注解)\n" +
                "5.1.3 [@Update注解](用于定义更新语句的注解)\n" +
                "\n" +
                "5.2 [基于注解的关联查询](通过注解实现关联关系查询)\n" +
                "5.2.1 [一对一查询](使用注解处理一对一关系查询)\n" +
                "5.2.2 [一对多查询](使用注解处理一对多关系查询)\n" +
                "5.2.3 [多对多查询](使用注解处理多对多关系查询)\n" +
                "\n" +
                "6 [初识Spring框架]\n" +
                "6.1 [Spring介绍](Spring框架的基本概念和优势)\n" +
                "6.1.1 [Spring概述](Spring框架的核心特性与应用领域)\n" +
                "6.1.2 [Spring框架的优点](简化开发、提供容器管理、事务管理等功能)\n" +
                "6.1.3 [Spring体系结构](Spring核心模块组成及其关系)\n" +
                "\n" +
                "6.2 [Spring的入门程序](Spring框架的基本使用方法)\n" +
                "6.2.1 [环境搭建](配置Spring开发环境及相关依赖)\n" +
                "6.2.2 [核心概念](Spring容器及Bean的基本使用)\n" +
                "6.2.3 [配置文件](使用XML配置Spring Bean)\n" +
                "\n" +
                "7 [Spring中的Bean管理]\n" +
                "7.1 [Spring IOC容器](控制反转容器的核心概念和类型)\n" +
                "7.1.1 [BeanFactory接口](Spring容器的核心接口)\n" +
                "7.1.2 [ApplicationContext接口](功能更强大的容器类型)\n" +
                "7.1.3 [容器启动过程](Spring容器初始化与Bean加载过程)\n" +
                "\n" +
                "7.2 [Bean的配置](Bean配置方式及属性设置)\n" +
                "7.2.1 [XML配置方式](通过XML文件配置Bean属性)\n" +
                "7.2.2 [注解配置方式](使用注解方式配置Bean)\n" +
                "7.2.3 [自动装配](自动化Bean依赖注入机制)\n" +
                "\n" +
                "8 [Spring AOP]\n" +
                "8.1 [Spring AOP介绍](面向切面编程的概述和实现原理)\n" +
                "8.1.1 [AOP概念](切面、连接点、切入点等基本概念)\n" +
                "8.1.2 [AOP术语](通知、切面、切入点等相关术语解释)\n" +
                "8.1.3 [AOP应用](在企业级应用中的使用场景和优势)\n" +
                "\n" +
                "8.2 [Spring AOP实现机制](AOP在Spring中的具体实现方法)\n" +
                "8.2.1 [JDK动态代理](使用JDK反射机制实现AOP)\n" +
                "8.2.2 [CGLib动态代理](通过字节码生成实现AOP)\n" +
                "8.2.3 [代理机制比较](代理方式的差异和适用场景)\n" +
                "\n" +
                "9 [Spring的数据库编程]\n" +
                "9.1 [Spring JDBC](Spring对JDBC的封装和改进)\n" +
                "9.1.1 [JdbcTemplate概述](JdbcTemplate的功能及优势)\n" +
                "9.1.2 [Spring JDBC配置](JDBC连接池的配置方法)\n" +
                "9.1.3 [JDBC常用方法](execute、update、query等方法详解)\n" +
                "\n" +
                "9.2 [JdbcTemplate的常用方法](JdbcTemplate的操作方法和使用技巧)\n" +
                "9.2.1 [execute()方法](执行DDL语句的通用方法)\n" +
                "9.2.2 [update()方法](执行DML语句的更新方法)\n" +
                "9.2.3 [query()方法](执行查询语句并返回结果的方法)\n" +
                "\n" +
                "10 [初识Spring MVC框架]\n" +
                "10.1 [Spring MVC介绍](Spring MVC框架的概述和特点)\n" +
                "10.1.1 [Spring MVC概述](MVC模式在Web开发中的应用)\n" +
                "10.1.2 [Spring MVC特点](松耦合、可扩展性强等优势)\n" +
                "10.1.3 [Spring MVC工作原理](请求处理流程分析)\n" +
                "\n" +
                "10.2 [Spring MVC入门程序](Spring MVC的基本使用流程)\n" +
                "10.2.1 [控制器实现](@Controller注解的使用方法)\n" +
                "10.2.2 [请求映射处理](@RequestMapping注解功能详解)\n" +
                "10.2.3 [页面响应](返回视图名称和处理响应数据)\n" +
                "\n" +
                "11 [Spring MVC的核心类和注解]\n" +
                "11.1 [DispatcherServlet](前端控制器的配置和工作原理)\n" +
                "11.1.1 [核心功能](请求分发和处理的核心组件)\n" +
                "11.1.2 [配置方法](在web.xml中的配置方式)\n" +
                "11.1.3 [工作机制](请求处理流程说明)\n" +
                "\n" +
                "11.2 [@Controller注解](控制器类的标识和使用方法)\n" +
                "11.2.1 [注解作用](标记控制器类)\n" +
                "11.2.2 [类中方法配置](方法的业务逻辑处理)\n" +
                "11.2.3 [路由方式](通过注解方式配置请求映射)\n" +
                "\n" +
                "11.3 [@RequestMapping注解](请求映射配置的核心注解)\n" +
                "11.3.1 [注解使用](基础请求路径映射设置)\n" +
                "11.3.2 [注解属性](参数支持和映射方式配置)\n" +
                "11.3.3 [请求映射方式](GET、POST等不同请求方式处理)\n" +
                "\n" +
                "12 [Spring MVC数据绑定和响应]\n" +
                "12.1 [数据绑定](请求参数到控制器方法参数的绑定过程)\n" +
                "12.1.1 [默认类型数据绑定](基本类型参数自动绑定)\n" +
                "12.1.2 [简单数据类型绑定](String等简单类型参数绑定)\n" +
                "12.1.3 [POJO绑定](对象属性的自动绑定机制)\n" +
                "\n" +
                "12.2 [复杂数据绑定](嵌套对象及集合类型的数据绑定)\n" +
                "12.2.1 [数组绑定](数组类型参数处理)\n" +
                "12.2.2 [集合绑定](List类型参数处理)\n" +
                "12.2.3 [复杂POJO绑定](复杂对象属性映射)\n" +
                "\n" +
                "12.3 [页面跳转](控制视图跳转和返回结果的方式)\n" +
                "12.3.1 [void类型返回值](页面跳转及渲染控制)\n" +
                "12.3.2 [String类型返回值](视图名称返回及跳转)\n" +
                "12.3.3 [ModelAndView类型返回值](视图和数据同时返回)\n" +
                "\n" +
                "13 [Spring MVC的高级功能]\n" +
                "13.1 [异常处理](处理请求异常情况的配置方式)\n" +
                "13.1.1 [简单异常处理器](使用@ExceptionHandler进行异常处理)\n" +
                "13.1.2 [自定义异常处理器](自定义异常处理类)\n" +
                "13.1.3 [异常处理注解](@ControllerAdvice和@ExceptionHandler组合)\n" +
                "\n" +
                "13.2 [拦截器](对请求进行预处理和后处理的组件)\n" +
                "13.2.1 [拦截器概述](拦截器机制和作用描述)\n" +
                "13.2.2 [拦截器配置](配置拦截器的生效路径)\n" +
                "13.2.3 [拦截器执行流程](拦截器内部的执行生命周期)\n" +
                "\n" +
                "14 [SSM框架整合]\n" +
                "14.1 [常用方式整合](传统Spring+Spring MVC+MyBatis整合方式)\n" +
                "14.1.1 [整合思路](整合各框架的关键点)\n" +
                "14.1.2 [项目基础结构搭建](目录结构规划与配置文件整合)\n" +
                "14.1.3 [Spring和MyBatis整合](数据访问层整合配置)\n" +
                "14.1.4 [Spring和Spring MVC整合](Web层整合方式)\n" +
                "\n" +
                "14.2 [纯注解方式整合](完全使用注解配置整合框架)\n" +
                "14.2.1 [整合思路](使用注解配置整合方案)\n" +
                "14.2.2 [纯注解SSM框架整合](完整注解配置示例)\n" +
                "\n" +
                "15 [云借阅图书管理系统]\n" +
                "15.1 [系统概述](系统整体功能和架构设计)\n" +
                "15.1.1 [系统功能介绍](图书管理、用户登录等功能)\n" +
                "15.1.2 [系统架构设计](MVC模式下的系统设计)\n" +
                "15.1.3 [文件组织结构](项目文件结构布局)";

        String jsonResult = convertTextToJson(inputText);
        System.out.println(jsonResult);
    }

    public static String convertTextToJson(String inputText) {
        // 定义正则表达式匹配模式
//        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)*) \\$(.*?)\\$(?:\\$(.*?)\\$)?");
        Pattern pattern = Pattern.compile("(\\d+(?:\\.\\d+)*) \\[(.*?)\\](?:\\((.*?)\\))?");
        // 分割输入文本为行
        String[] lines = inputText.split("\n");

        // 准备结果数据
        String knowpointPlan = "";
        List<KnowledgePoint> knowledgePoints = new ArrayList<>();

        // 处理每一行
        for (String line : lines) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                String chap = matcher.group(1);
                String knowpName = matcher.group(2);
                String description = matcher.group(3) != null ? matcher.group(3) : "";

                System.out.println(chap);
                System.out.println(knowpName);
                System.out.println(description);


                // 生成knpId（转换为整数形式，如1.1.1 -> 111）

                // 确定父节点ID
                String knowpointParent = getParentId(chap);

                // 确定节点层级
                String knowpExpand = String.valueOf(chap.split("\\.").length);

                // 创建知识点对象
                KnowledgePoint kp = new KnowledgePoint(
                        chap,
                        knowpName,
                        knowpointParent,
                        knowpExpand,
                        chap,
                        description
                );
                knowledgePoints.add(kp);
            }
        }

        // 构建JSON对象
        JSONObject result = new JSONObject();
        result.put("knowpoint_plan", knowpointPlan);

        JSONArray pointsArray = new JSONArray();
        for (KnowledgePoint kp : knowledgePoints) {
            JSONObject kpJson = new JSONObject();
            kpJson.put("knpId", kp.getKnpId());
            kpJson.put("knowpName", kp.getKnowpName());
            kpJson.put("knowpointParent", kp.getKnowpointParent());
            kpJson.put("knowpExpand", kp.getKnowpExpand());
            kpJson.put("chap", kp.getChap());
            kpJson.put("description", kp.getDescription());
            pointsArray.add(kpJson);
        }
        result.put("knowledgePoints", pointsArray);
        return result.toString(); // 缩进2个空格，美化输出
    }



    // 获取父节点ID
    private static String getParentId(String chap) {
        String[] parts = chap.split("\\.");
        if (parts.length <= 1) return "0"; // 顶级节点没有父节点

        // 获取父节点章节（去掉最后一部分）
        StringBuilder parentChap = new StringBuilder();
        for (int i = 0; i < parts.length - 1; i++) {
            if (i > 0) parentChap.append(".");
            parentChap.append(parts[i]);
        }

        return parentChap.toString();
    }

    // 知识点内部类
    static class KnowledgePoint {
        private String knpId;
        private String knowpName;
        private String knowpointParent;
        private String knowpExpand;
        private String chap;
        private String description;

        public KnowledgePoint(String knpId, String knowpName, String knowpointParent,
                              String knowpExpand, String chap, String description) {
            this.knpId = knpId;
            this.knowpName = knowpName;
            this.knowpointParent = knowpointParent;
            this.knowpExpand = knowpExpand;
            this.chap = chap;
            this.description = description;
        }

        // Getter方法
        public String getKnpId() { return knpId; }
        public String getKnowpName() { return knowpName; }
        public String getKnowpointParent() { return knowpointParent; }
        public String getKnowpExpand() { return knowpExpand; }
        public String getChap() { return chap; }
        public String getDescription() { return description; }
    }
}