/**
 * ChainedCollections.java
 * ---------------------------------
 * Copyright (c) 2014
 * RESOLVE Software Research Group
 * School of Computing
 * Clemson University
 * All rights reserved.
 * ---------------------------------
 * This file is subject to the terms and conditions defined in
 * file 'LICENSE.txt', which is part of this source code package.
 */
package edu.clemson.cs.r2jt.utilities;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

import edu.clemson.cs.r2jt.proving.ChainingIterator;

public class ChainedCollections<T> extends AbstractCollection<T> {

    private Collection<T> myFirst;
    private Collection<T> mySecond;
    private Collection<T> myPersonalCollection = new LinkedList<T>();

    public ChainedCollections(Collection<T> first, Collection<T> second) {
        myFirst = first;
        mySecond = second;
    }

    @Override
    public Iterator<T> iterator() {
        return new ChainingIterator<T>(
                myPersonalCollection.iterator(),
                new ChainingIterator<T>(myFirst.iterator(), mySecond.iterator()));
    }

    @Override
    public int size() {
        return myFirst.size() + mySecond.size() + myPersonalCollection.size();
    }

    @Override
    public boolean add(T t) {
        myPersonalCollection.add(t);

        return true;
    }
}
