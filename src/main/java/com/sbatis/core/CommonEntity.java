package com.sbatis.core;

import com.sbatis.core.annotation.Column;

import java.math.BigInteger;
import java.util.Date;

/**
 * 公共基础实体，所有定义的实体都需要继承此类
 * @author HuangLongPu
 */
public class CommonEntity {

	/**
	 * ID 主键，唯一性标识
	 */
	private BigInteger id;
	/**
	 * 创建时间
	 */
	@Column(name = CommonField.FIELD_CREATE_TIME)
	private Date createTime;

	public BigInteger getId() {
		return id;
	}
	public void setId(BigInteger id) {
		this.id = id;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

}
