package com.imooc.myo2o.dao.split;

import java.util.Locale;

import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * mybatis会将增删改的操作封装到update这个method里面
 * 
 * @author Administrator
 *
 */
@Intercepts({ @Signature(type = Executor.class, method = "update", args = { MappedStatement.class, Object.class }),
		@Signature(type = Executor.class, method = "query", args = { MappedStatement.class, Object.class,
				RowBounds.class, ResultHandler.class }), })
public class DynamicDataSourceInteceptor implements Interceptor {

	private static Logger logger = LoggerFactory.getLogger(DynamicDataSourceInteceptor.class);

	private static final String REGEX = ".*insert\\u0020.*|.*delete\\u0020.*|.*update\\u0020.*";

	@Override
	public Object intercept(Invocation invocation) throws Throwable {
		// 判断当前是否是事务
		boolean syncActive = TransactionSynchronizationManager.isActualTransactionActive();

		Object[] objects = invocation.getArgs();
		MappedStatement ms = (MappedStatement) objects[0];
		String lookupkey = DynamicDataSourceHolder.DB_MASTER;

		if (!syncActive) {
			// 读方法
			if (ms.getSqlCommandType().equals(SqlCommandType.SELECT)) {
				// selectKey 为自增id查询主键(SELECT LAST_INSERT_ID())方法,使用主库
				if (ms.getId().contains(SelectKeyGenerator.SELECT_KEY_SUFFIX)) {
					lookupkey = DynamicDataSourceHolder.DB_MASTER;
				} else {
					BoundSql boundSql = ms.getSqlSource().getBoundSql(objects[1]);
					String sql = boundSql.getSql().toLowerCase(Locale.CHINA).replaceAll("[\\t\\n\\r]", " ");
					if (sql.matches(REGEX)) {
						lookupkey = DynamicDataSourceHolder.DB_MASTER;
					} else {
						lookupkey = DynamicDataSourceHolder.DB_SLAVE;
					}
				}
			}
		} else {
			lookupkey = DynamicDataSourceHolder.DB_MASTER;
		}
		logger.debug("设置方法[{}] use[{}] Strategy, SqlCommondType[{}]...", ms.getId(), lookupkey,
				ms.getSqlCommandType().name());
		DynamicDataSourceHolder.setDbType(lookupkey);
		return invocation.proceed();
	}

	@Override
	public Object plugin(Object target) {
		/**
		 * 当拦截的类型是Executor的时候就作拦截,其他类型不拦截. 其他类型包括 ParameterHandler
		 * ResultSetHandler StatementHandler 原因是Executor在mybatis中支持增删改查操作
		 */
		if (target instanceof Executor) {
			return Plugin.wrap(target, this);
		} else {
			return target;
		}
	}

	@Override
	public void setProperties(Properties properties) {
		// TODO Auto-generated method stub

	}

}
