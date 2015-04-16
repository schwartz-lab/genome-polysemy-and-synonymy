/*
 * Copyright (C) 2011 Free Software Foundation, Inc.
 * This file is part of the MobSim Project.
 *
 * The MobSim Project is free software; you can redistribute it
 * and/or modify it under the terms of the GNU Library General Public
 * License as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * The MobSim Project  is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with the MobSim Project ; see the file COPYING.LIB.
 * If not, write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA 02110-1301, USA.
 */

package edu.wisc.lmcg.svg;

import java.util.List;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import org.w3c.dom.Attr;

/**
 *
 * @author David Saldaña
 */
public class DynamicElement {
        // centro del elemento
        String idElement;
        double scaleX;
        double scaleY; 
        Point2D position;
        List<Attr> attributes;
        double angle = 0;

        public DynamicElement(String idElement, Point2D position, double angle, double scaleX, double scaleY) {            
                this.attributes = new ArrayList<>();
                this.scaleY = scaleY;
                this.scaleX = scaleX;
                this.position = position;
                this.angle = angle;
                this.idElement = idElement;
        }
        
        public DynamicElement(String idElement, Point2D position, double angle) {
                this(idElement, position, angle, 1.0, 1.0);
        }

        public DynamicElement(Point2D position, double angle) {
                this("", position, angle);                
        }
        
        public DynamicElement(Point2D position) {
                this("", position, 0.0);                
        }
        
        public DynamicElement() {
            this(new Point2D.Double(0.0, 0.0));
        }

        public void setPosition(Point2D position) {
                this.position = position;
        }

        public Point2D getPosition() {
                return position;
        }

        public void setAngle(double angulo) {
                this.angle = angulo;
        }

        public double getAngle() {
                return angle;
        }

        public String getIdElement() {
                return idElement;
        }

        public void setIdElement(String idElement) {
                this.idElement = idElement;
        }
        
        public double getScaleX() {
                return scaleX;
        }
        
        public double getScaleY() {
                return scaleY;
        }
        
        public void setScaleX(double value) {
                this.scaleX = value;
        }
        
        public void setScaleY(double value) {
                this.scaleY = value;
        }
        
        public void setAttribute(Attr v){
            for( int i=0; i < attributes.size(); i++){
                Attr a = attributes.get(i);
                if ( a.getName().equals(v.getName()) )
                {
                    attributes.set(i, v);
                    return;
                }
            }
            
            attributes.add(v);
        }
        
        public Iterable<Attr> getAttributes(){
            return attributes;
        }
        
        public void removeAttribute(Attr v){
            if ( attributes.contains(v))
                attributes.remove(v);
        }
}
