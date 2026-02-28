package com.dat.whmanagement.dao;

import com.dat.whmanagement.model.StockItem;

import java.util.List;

public interface StockDAO {
    List<StockItem> findAll();
    void updateStock(int productId, double quantityDelta);
}

