package com.itu.socialcom.demo.products.dto;

import com.itu.socialcom.demo.products.model.Category;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class DisplayContainer {
    CreationStepsDTO creationStepsDTO;
    List<Category> categories;
}
