package com.jia.config;/**
 * @author ChenJia
 * @create 2024-01-09 9:07
 */

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *@ClassName NoticeWebsocketResp
 *@Description TODO
 *@Author jia
 *@Date 2024/1/9 9:07
 *@Version 1.0
 **/

@Data
@ApiModel("ws通知返回对象")
public class NoticeWebsocketResp<T> {
    @ApiModelProperty(value = "通知类型")
    private String noticeType;

    @ApiModelProperty(value = "通知内容")
    private T noticeInfo;
}
