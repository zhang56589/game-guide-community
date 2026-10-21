package com.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * 审计日志实体
 */
public class AuditLog implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long    id;
    private String  operator;    // 操作人账号
    private String  nickname;    // 操作人昵称
    private String  method;      // com.service.impl.UserServiceImpl.login
    private String  params;      // 入参JSON字符串
    private Integer durationMs;  // 耗时(ms)
    private Integer success;     // 1=成功 0=异常
    private String  errorMsg;    // 异常信息
    private Date    createTime;

    public AuditLog() {}

    public Long    getId()          { return id; }
    public void    setId(Long id)   { this.id = id; }

    public String  getOperator()    { return operator; }
    public void    setOperator(String operator) { this.operator = operator; }

    public String  getNickname()    { return nickname; }
    public void    setNickname(String nickname) { this.nickname = nickname; }

    public String  getMethod()      { return method; }
    public void    setMethod(String method) { this.method = method; }

    public String  getParams()      { return params; }
    public void    setParams(String params) { this.params = params; }

    public Integer getDurationMs()  { return durationMs; }
    public void    setDurationMs(Integer durationMs) { this.durationMs = durationMs; }

    public Integer getSuccess()     { return success; }
    public void    setSuccess(Integer success) { this.success = success; }

    public String  getErrorMsg()    { return errorMsg; }
    public void    setErrorMsg(String errorMsg) { this.errorMsg = errorMsg; }

    public Date    getCreateTime()  { return createTime; }
    public void    setCreateTime(Date createTime) { this.createTime = createTime; }
}
