package com.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * 攻略实体类
 */
public class Strategy implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer id;          // 主键
    private String  title;       // 标题
    private String  gameName;    // 游戏名称
    private String  content;     // 内容
    private String  coverImage;  // 封面图
    private Integer userId;      // 作者ID
    private Integer categoryId;  // 分类ID
    private Integer viewCount;   // 浏览量
    private Integer likeCount;   // 点赞数
    private Integer status;      // 0=已删, 1=正常
    private Integer auditStatus; // 0=待审核 1=已通过 2=已拒绝
    private Date    createTime;  // 发布时间
    private Date    updateTime;  // 更新时间

    // ---------- 附加字段(查询填充) ----------
    private String  authorName;   // 作者昵称
    private String  categoryName; // 分类名

    public Strategy() {}

    // ========== Getter / Setter ==========
    public Integer getId()           { return id; }
    public void    setId(Integer id) { this.id = id; }

    public String  getTitle()        { return title; }
    public void    setTitle(String title) { this.title = title; }

    public String  getGameName()     { return gameName; }
    public void    setGameName(String gameName) { this.gameName = gameName; }

    public String  getContent()      { return content; }
    public void    setContent(String content) { this.content = content; }

    public String  getCoverImage()    { return coverImage; }
    public void    setCoverImage(String coverImage) { this.coverImage = coverImage; }

    public Integer getUserId()       { return userId; }
    public void    setUserId(Integer userId) { this.userId = userId; }

    public Integer getCategoryId()   { return categoryId; }
    public void    setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public Integer getViewCount()    { return viewCount; }
    public void    setViewCount(Integer viewCount) { this.viewCount = viewCount; }

    public Integer getLikeCount()    { return likeCount; }
    public void    setLikeCount(Integer likeCount) { this.likeCount = likeCount; }

    public Integer getStatus()       { return status; }
    public void    setStatus(Integer status) { this.status = status; }

    public Integer getAuditStatus() { return auditStatus; }
    public void    setAuditStatus(Integer auditStatus) { this.auditStatus = auditStatus; }

    public Date    getCreateTime()   { return createTime; }
    public void    setCreateTime(Date createTime) { this.createTime = createTime; }

    public Date    getUpdateTime()   { return updateTime; }
    public void    setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

    public String  getAuthorName()   { return authorName; }
    public void    setAuthorName(String authorName) { this.authorName = authorName; }

    public String  getCategoryName() { return categoryName; }
    public void    setCategoryName(String categoryName) { this.categoryName = categoryName; }
}
