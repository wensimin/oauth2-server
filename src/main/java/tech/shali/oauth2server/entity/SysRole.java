package tech.shali.oauth2server.entity;


import tech.shali.oauth2server.entity.base.DataEntity;

import javax.persistence.Column;
import javax.persistence.Entity;

/**
 * 角色对象
 *
 * @author wensimin
 */
@Entity
public class SysRole extends DataEntity {
    private static final long serialVersionUID = 1L;

    @Column(nullable = false, unique = true)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
