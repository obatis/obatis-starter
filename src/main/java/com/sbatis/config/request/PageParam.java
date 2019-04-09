package com.sbatis.config.request;

import com.sbatis.core.annotation.validator.NotZero;
import com.sbatis.core.annotation.validator.OrderValid;
import io.swagger.annotations.ApiModelProperty;

public class PageParam extends RequestParam {

	@ApiModelProperty(value = "当前页码")
	private int page = RequestConstant.DEFAULT_PAGE;
	@ApiModelProperty(value = "显示行数")
	@NotZero(message = "显示行数不能为0")
	private int rows = RequestConstant.DEFAULT_ROWS;
	@ApiModelProperty(value = "排序字段")
	private String sort;
	@ApiModelProperty(value = "排序方式，0:升序 1:降序，默认升序。只接收1和2，其他无效")
	@OrderValid
	private int order;
	
	public final Integer getPage() {
		return page;
	}
	public final void setPage(Integer page) {
		this.page = page;
	}
	public final Integer getRows() {
		return rows;
	}
	public final void setRows(Integer rows) {
		this.rows = rows;
	}
	public final String getSort() {
		return sort;
	}
	public final void setSort(String sort) {
		this.sort = sort;
	}
	public final int getOrder() {
		return order;
	}
	public final void setOrder(int order) {
		this.order = order;
	}
	
}
