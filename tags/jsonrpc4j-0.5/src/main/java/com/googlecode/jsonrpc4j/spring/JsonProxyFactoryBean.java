package com.googlecode.jsonrpc4j.spring;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.remoting.support.UrlBasedRemoteAccessor;

import com.googlecode.jsonrpc4j.JsonRpcHttpClient;


public class JsonProxyFactoryBean 
	extends UrlBasedRemoteAccessor 
	implements MethodInterceptor,
	InitializingBean,
	FactoryBean {
	
	private Object proxyObject 			= null;
	private ObjectMapper objectMapper 	= null;
	private JsonRpcHttpClient jsonRpcHttpClient	= null;
	private Map<String, String> extraHttpHeaders = new HashMap<String, String>();
	private ApplicationContext applicationContext;
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void afterPropertiesSet() {
		super.afterPropertiesSet();
		
		// create proxy
		proxyObject = ProxyFactory.getProxy(getServiceInterface(), this);

    	// find the ObjectMapper
		if (objectMapper==null
			&& applicationContext!=null
			&& applicationContext.containsBean("objectMapper")) {
			objectMapper = (ObjectMapper)applicationContext.getBean("objectMapper");
		}
		if (objectMapper==null
			&& applicationContext!=null) {
			try {
				objectMapper = (ObjectMapper)BeanFactoryUtils.beanOfTypeIncludingAncestors(
					applicationContext, ObjectMapper.class);
			} catch(Exception e) {
				objectMapper = new ObjectMapper();
			}
		}

		// create JsonRpcHttpClient
		try {
			jsonRpcHttpClient = new JsonRpcHttpClient(
				objectMapper, new URL(getServiceUrl()), extraHttpHeaders);
		} catch(MalformedURLException mue) {
			throw new RuntimeException(mue);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public Object invoke(MethodInvocation invocation) 
		throws Throwable {

		// invoke it
		return jsonRpcHttpClient.invoke(
			invocation.getMethod().getName(),
			invocation.getArguments(),
			invocation.getMethod().getReturnType());
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Object getObject() 
		throws Exception {
		return proxyObject;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Class<?> getObjectType() {
		return getServiceInterface();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isSingleton() {
		return true;
	}

	/**
	 * @param objectMapper the objectMapper to set
	 */
	public void setObjectMapper(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	/**
	 * @param extraHttpHeaders the extraHttpHeaders to set
	 */
	public void setExtraHttpHeaders(Map<String, String> extraHttpHeaders) {
		this.extraHttpHeaders = extraHttpHeaders;
	}

	public void setApplicationContext(ApplicationContext applicationContext)
		throws BeansException {
		this.applicationContext = applicationContext;
	}

}
