package com.aren.orderserver.entities;

import com.aren.orderserver.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Entity
@Table(name = "orders")
@NoArgsConstructor
@Getter
@Setter
public class Order implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "status")
    private String status;

    @JoinColumn(name = "created_by")
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private User createdBy;

    @JoinColumn(name = "updated_by")
    @ManyToOne(cascade = {CascadeType.MERGE, CascadeType.DETACH, CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH})
    private User processedBy;

    @Column(name = "created_date")
    private OffsetDateTime createdDate;

    @Column(name = "updated_date")
    private OffsetDateTime updatedDate;

    public Order(String title,
                 String description,
                 String status,
                 User createdBy,
                 User processedBy,
                 OffsetDateTime createdDate,
                 OffsetDateTime updatedDate) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.createdBy = createdBy;
        this.processedBy = processedBy;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
    }
}
