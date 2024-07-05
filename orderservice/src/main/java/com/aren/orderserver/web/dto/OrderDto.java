package com.aren.orderserver.web.dto;

import com.aren.orderserver.entities.User;
import com.aren.orderserver.web.validation.OnCreate;
import com.aren.orderserver.web.validation.OnUpdate;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

@Data
public class OrderDto {

    @NotNull(message = "Id must not be null", groups = OnUpdate.class)
    private Integer id;

    @NotNull(message = "Title must not be null", groups = {OnUpdate.class, OnCreate.class})
    @Length(max = 255, message = "Title must not be bigger than 255 symbols", groups = {OnUpdate.class, OnCreate.class})
    private String title;

    @Length(max = 255, message = "Description must not be bigger than 255 symbols", groups = {OnUpdate.class, OnCreate.class})
    private String description;

    private String status;

    private User createdBy;

    private User processedBy;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private OffsetDateTime createdDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private OffsetDateTime updatedDate;

}
