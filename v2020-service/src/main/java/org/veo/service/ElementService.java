/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.veo.service;

import java.util.List;

import org.veo.model.Element;

/**
 *
 * @author Daniel Murygin
 */
public interface ElementService {
    
    public Element save(Element element);

    public Element load(String uuid);
    
    public List<Element> loadAll(String typeId);
}
