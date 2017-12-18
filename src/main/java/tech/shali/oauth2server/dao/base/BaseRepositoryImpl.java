package tech.shali.oauth2server.dao.base;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.query.QueryUtils;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.LockModeType;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.FetchParent;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 基础存储库
 *
 * @param <T>
 * @param <ID>
 * @author wensimin
 */
public class BaseRepositoryImpl<T, ID extends Serializable> extends SimpleJpaRepository<T, ID> {

    private final EntityManager em;
    protected JpaEntityInformation<T, ?> entityInformation;

    public BaseRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.em = entityManager;
        this.entityInformation = entityInformation;
    }

    @Override
    protected <S extends T> TypedQuery<S> getQuery(Specification<S> spec, Class<S> domainClass, Sort sort) {
        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<S> query = builder.createQuery(domainClass);
        Root<S> root = applySpecificationToCriteria(spec, domainClass, query);
        query.select(root);
        applyFetchMode(root);
        if (sort != null) {
            query.orderBy(QueryUtils.toOrders(sort, root, builder));
        }
        return applyRepositoryMethodMetadata(em.createQuery(query));
    }

    /**
     * 关联map
     */
    private Map<String, Join<?, ?>> joinCache;

    private void applyFetchMode(Root<? extends T> root) {
        joinCache = new HashMap<>();
        applyFetchMode(root, getDomainClass(), "");
    }

    /**
     * 应用fetch mode
     *
     * @param root  root
     * @param clazz 字段类型
     * @param path  路径
     */
    private void applyFetchMode(FetchParent<?, ?> root, Class<?> clazz, String path) {
        for (Field field : clazz.getDeclaredFields()) {
            Fetch fetch = field.getAnnotation(Fetch.class);
            // 如果 fetch未显式指定，则按照eager的默认设置来进行join
            if (fetch == null) {
                ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
                OneToOne oneToOne = field.getAnnotation(OneToOne.class);
                boolean manyToOneEager = manyToOne != null && manyToOne.fetch() == FetchType.EAGER;
                boolean oneToOneEager = oneToOne != null && oneToOne.fetch() == FetchType.EAGER;
                if (manyToOneEager || oneToOneEager) {
                    applyFetchMode(root, path, field, !field.getType().equals(clazz));
                }
            } else if (fetch.value() == FetchMode.JOIN) {
                applyFetchMode(root, path, field, !field.getType().equals(clazz));
            }
        }
    }

    private void applyFetchMode(FetchParent<?, ?> root, String path, Field field, boolean recursive) {
        FetchParent<?, ?> descent = root.fetch(field.getName(), JoinType.LEFT);
        String fieldPath = path + "." + field.getName();
        joinCache.put(path, (Join<?, ?>) descent);
        if (recursive) {
            applyFetchMode(descent, field.getType(), fieldPath);
        }
    }

    /**
     * Applies the given {@link Specification} to the given
     * {@link CriteriaQuery}.
     *
     * @param spec        can be {@literal null}.
     * @param domainClass must not be {@literal null}.
     * @param query       must not be {@literal null}.
     * @return root
     */
    private <S, U extends T> Root<U> applySpecificationToCriteria(Specification<U> spec, Class<U> domainClass,
                                                                  CriteriaQuery<S> query) {

        Assert.notNull(domainClass, "Domain class must not be null!");
        Assert.notNull(query, "CriteriaQuery must not be null!");

        Root<U> root = query.from(domainClass);

        if (spec == null) {
            return root;
        }

        CriteriaBuilder builder = em.getCriteriaBuilder();
        Predicate predicate = spec.toPredicate(root, query, builder);

        if (predicate != null) {
            query.where(predicate);
        }

        return root;
    }

    private <S> TypedQuery<S> applyRepositoryMethodMetadata(TypedQuery<S> query) {
        if (getRepositoryMethodMetadata() == null) {
            return query;
        }

        LockModeType type = getRepositoryMethodMetadata().getLockModeType();
        TypedQuery<S> toReturn = type == null ? query : query.setLockMode(type);

        applyQueryHints(toReturn);

        return toReturn;
    }

    private void applyQueryHints(Query query) {
        for (Map.Entry<String, Object> hint : getQueryHints().entrySet()) {
            query.setHint(hint.getKey(), hint.getValue());
        }
    }

    public Class<T> getEntityType() {
        return entityInformation.getJavaType();
    }

    public EntityManager getEm() {
        return em;
    }
}