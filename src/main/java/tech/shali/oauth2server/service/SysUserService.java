package tech.shali.oauth2server.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tech.shali.oauth2server.dao.UserDao;
import tech.shali.oauth2server.entity.SysUser;

@Service
public class SysUserService implements UserDetailsService {

    private UserDao userDao;

    private PasswordEncoder passwordEncoder;

    public SysUserService(UserDao userDao, PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userDao.findByUsername(username);
    }

    public SysUser create(SysUser sysUser) {
        sysUser.setPassword(passwordEncoder.encode(sysUser.getPassword()));
        return userDao.saveAndFlush(sysUser);
    }
}
