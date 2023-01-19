package com.ducheng.distributed.dynamic.schedule.utils;


import org.springframework.core.env.ConfigurablePropertyResolver;
import org.springframework.expression.Expression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class StrUtil {

   private static final SpelExpressionParser spelExpressionParser = new SpelExpressionParser();
   private static final TemplateParserContext templateParserContext= new TemplateParserContext();

    public static String joinStr(String... str) {
        StringBuilder sb = new StringBuilder();
        for (String sign : str) {
            if (null != sign)
                sb.append(sign);
        }
        return sb.toString();
    }

    /**
     *  动态占位符 替换
     * @param valueResolver
     * @return
     */
    public static String  resolve(ConfigurablePropertyResolver propertyResolver ,String valueResolver) {
        // 读取属性值
        String value = propertyResolver.resolveRequiredPlaceholders(valueResolver);
        Expression expression = spelExpressionParser.parseExpression(value, templateParserContext);
        return  expression.getValue().toString();
    }
}
