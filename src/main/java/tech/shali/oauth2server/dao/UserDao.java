package tech.shali.oauth2server.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import tech.shali.oauth2server.entity.SysUser;

public interface UserDao extends JpaRepository<SysUser, String>, JpaSpecificationExecutor<SysUser> {

	SysUser findByUsername(String username);
}
