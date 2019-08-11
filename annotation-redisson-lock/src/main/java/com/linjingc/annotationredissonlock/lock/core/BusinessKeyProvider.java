package com.linjingc.annotationredissonlock.lock.core;

import com.linjingc.annotationredissonlock.lock.annotation.CatLock;
import com.linjingc.annotationredissonlock.lock.annotation.LockKey;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * 获取用户定义业务key
 *
 * @author cxc
 * @date 2019年08月08日20:52:40
 */
@Component
public class BusinessKeyProvider {

    private ParameterNameDiscoverer nameDiscoverer = new DefaultParameterNameDiscoverer();

    private ExpressionParser parser = new SpelExpressionParser();

    public String getKeyName(ProceedingJoinPoint joinPoint, CatLock catLock) {
        List<String> keyList = new ArrayList<>();
        Method method = getMethod(joinPoint);
        //获取方法CatLock注解上的自定义keys
        List<String> definitionKeys = getSpelDefinitionKey(catLock.keys(), method, joinPoint.getArgs());
        keyList.addAll(definitionKeys);
        //获取参数注解LockKey 上的内容
        List<String> parameterKeys = getParameterKey(method.getParameters(), joinPoint.getArgs());
        keyList.addAll(parameterKeys);
        //进行拼接
        return StringUtils.collectionToDelimitedString(keyList, "", "-", "");
    }

    private Method getMethod(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        if (method.getDeclaringClass().isInterface()) {
            try {
                method = joinPoint.getTarget().getClass().getDeclaredMethod(signature.getName(), method.getParameterTypes());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return method;
    }

    /**
     * 获取方法CatLock注解上的自定义keys
     *
     * @param definitionKeys
     * @param method
     * @param parameterValues
     * @return
     */
    private List<String> getSpelDefinitionKey(String[] definitionKeys, Method method, Object[] parameterValues) {
        List<String> definitionKeyList = new ArrayList<>();
        for (String definitionKey : definitionKeys) {
            if (definitionKey != null && !definitionKey.isEmpty()) {
                EvaluationContext context = new MethodBasedEvaluationContext(null, method, parameterValues, nameDiscoverer);
                String key = parser.parseExpression(definitionKey).getValue(context).toString();
                definitionKeyList.add(key);
            }
        }
        return definitionKeyList;
    }


    /**
     * 获取参数注解LockKey 上的内容
     *
     * @param parameters
     * @param parameterValues
     * @return
     */
    private List<String> getParameterKey(Parameter[] parameters, Object[] parameterValues) {
        List<String> parameterKey = new ArrayList<>();
        //遍历参数
        for (int i = 0; i < parameters.length; i++) {
            //参数上带有注解
            if (parameters[i].getAnnotation(LockKey.class) != null) {
                LockKey keyAnnotation = parameters[i].getAnnotation(LockKey.class);
                //注解的内容不为空
                if (keyAnnotation.value().isEmpty()) {
                    parameterKey.add(parameterValues[i].toString());
                } else {
                    StandardEvaluationContext context = new StandardEvaluationContext(parameterValues[i]);
                    String key = parser.parseExpression(keyAnnotation.value()).getValue(context).toString();
                    parameterKey.add(key);
                }
            }
        }
        return parameterKey;
    }
}
