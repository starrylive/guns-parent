package com.stylefeng.guns.rest.modular.auth.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.stylefeng.guns.api.user.UserAPI;
import com.stylefeng.guns.core.exception.GunsException;
import com.stylefeng.guns.rest.common.exception.BizExceptionEnum;
import com.stylefeng.guns.rest.modular.auth.controller.dto.AuthRequest;
import com.stylefeng.guns.rest.modular.auth.controller.dto.AuthResponse;
import com.stylefeng.guns.rest.modular.auth.util.JwtTokenUtil;
import com.stylefeng.guns.rest.modular.auth.validator.IReqValidator;
import com.stylefeng.guns.rest.modular.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.stylefeng.guns.rest.modular.vo.ResponseVO.success;

/**
 * 请求验证的
 * 这里删掉了旧的实现(IReqValidator 和ResponseEntity<?>)，用自己写的userAPI和ResponseVO替代原来的实现
 * @author fengshuonan
 * @Date 2017/8/24 14:22
 */
@RestController
public class AuthController {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // check = false 关闭启动检查，否则没有开启user服务则本服务也无法启动
    @Reference(interfaceClass = UserAPI.class,check = false)
    private UserAPI userAPI;

    @RequestMapping(value = "${jwt.auth-path}")
    public ResponseVO createAuthenticationToken(AuthRequest authRequest) {

        boolean validate = true;
        // 去掉guns自身携带的用户名密码验证机制，使用我们自己的
        int userId = userAPI.login(authRequest.getUserName(),authRequest.getPassword());
        if(userId==0){
            validate = false;
        }

        if (validate) {
            // randomKey和token已经生成完毕
            final String randomKey = jwtTokenUtil.getRandomKey();
            // 尽量要用toString()，如果类型是Integer，且值为空，就会空指针
            final String token = jwtTokenUtil.generateToken(""+userId, randomKey);
            // 返回值
            return ResponseVO.success(new AuthResponse(token, randomKey));
        } else {
            return ResponseVO.serviceFail("用户名或密码错误");
        }
    }
}
