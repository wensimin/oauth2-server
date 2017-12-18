package tech.shali.oauth2server.entity.listeners;


import tech.shali.oauth2server.entity.base.DataEntity;

import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

/**
 * 公共实体的插入更新过滤
 */
public class DataEntityListener {

    @PrePersist
    public void methodExecuteBeforeInsert(final DataEntity reference) {
        reference.beforeInsert();
    }

    @PreUpdate
    public void methodExecuteBeforeUpdate(final DataEntity reference) {
        reference.beforeUpdate();
    }
}
