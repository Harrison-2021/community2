package com.nowcoder.community.dao;

import org.springframework.stereotype.Repository;

@Repository("Hibernate")
public class AlphaDaoHibernateImpl implements AlphaDAO{
    @Override
    public String select() {
        return "Hibernate";
    }
}
