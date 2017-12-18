package tech.shali.oauth2server.api;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import tech.shali.oauth2server.entity.SysUser;
import tech.shali.oauth2server.service.SysUserService;

import javax.validation.Valid;

/**
 * 注册controller
 */
@RestController
public class RegisterController {
    private SysUserService sysUserService;

    public RegisterController(SysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }
    @PostMapping("register")
    public SysUser create(@RequestBody @Valid  SysUser user){
        return sysUserService.create(user);
    }
}
