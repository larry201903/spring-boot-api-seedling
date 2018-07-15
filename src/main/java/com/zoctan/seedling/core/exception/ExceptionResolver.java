package com.zoctan.seedling.core.exception;

import com.zoctan.seedling.core.response.Result;
import com.zoctan.seedling.core.response.ResultCode;
import com.zoctan.seedling.core.response.ResultGenerator;
import com.zoctan.seedling.util.UrlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
import java.util.stream.Collectors;

/**
 * 统一异常处理
 * 对于业务异常：返回头 Http 状态码一律使用200，只在响应 Result 中指明异常的状态码 code
 *
 * @author Zoctan
 * @date 2018/06/09
 */
@RestControllerAdvice
public class ExceptionResolver {
    private final static Logger log = LoggerFactory.getLogger(ExceptionResolver.class);

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler(ConstraintViolationException.class)
    public Result validatorException(final ConstraintViolationException e) {
        log.error("验证实体异常 => {}", e.getMessage());
        final String msg = e.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(","));
        return ResultGenerator.genFailedResult(msg);
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler({ServiceException.class, ServletException.class})
    public Result serviceException(final Throwable e) {
        log.error("服务异常 => {}", e.getMessage());
        return ResultGenerator.genFailedResult(e.getMessage());
    }

    @ResponseStatus(HttpStatus.OK)
    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public Result databaseException(final Throwable e) {
        log.error("数据库异常 => {}", e.getMessage());
        return ResultGenerator.genFailedResult("database error");
    }

    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler({BadCredentialsException.class, AuthenticationException.class})
    public Result authException(final Throwable e) {
        log.error("身份验证异常 => {}", e.getMessage());
        return ResultGenerator.genFailedResult(ResultCode.UNAUTHORIZED);
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler({AccessDeniedException.class, UsernameNotFoundException.class})
    public Result accountException(final Throwable e) {
        log.error("账户异常 => {}", e.getMessage());
        return ResultGenerator.genFailedResult(e.getMessage());
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public Result apiNotFound(final Throwable e, final HttpServletRequest request) {
        log.error("API 不存在 => {}", e.getMessage());
        return ResultGenerator.genFailedResult("API [" + UrlUtils.getMappingUrl(request) + "] not existed");
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public Result globalException(final HttpServletRequest request, final Throwable e) {
        log.error("全局异常 => {}", e.getMessage());
        return ResultGenerator.genFailedResult(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                String.format("%s => %s", UrlUtils.getMappingUrl(request), e.getMessage()));
    }
}
