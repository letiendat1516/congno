package com.dat.whmanagement.service.impl;

import com.dat.whmanagement.dao.StockDAO;
import com.dat.whmanagement.dao.impl.StockDAOImpl;
import com.dat.whmanagement.model.StockItem;
import com.dat.whmanagement.service.StockService;

import java.util.List;

public class StockServiceImpl implements StockService {

    private final StockDAO dao;

    public StockServiceImpl() { this.dao = new StockDAOImpl(); }
    public StockServiceImpl(StockDAO dao) { this.dao = dao; }

    @Override
    public List<StockItem> getAll() { return dao.findAll(); }
}

