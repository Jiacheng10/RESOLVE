/**
 * LazyActionIterator.java
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
package edu.clemson.cs.r2jt.proving;

import java.util.Iterator;

public class LazyActionIterator<S, D> implements Iterator<D> {

    private Iterator<S> myInternalIterator;
    private Transformer<S, D> myAction;

    public LazyActionIterator(Iterable<S> i, Transformer<S, D> action) {
        this(i.iterator(), action);
    }

    public LazyActionIterator(Iterator<S> i, Transformer<S, D> action) {
        myInternalIterator = i;
        myAction = action;
    }

    public boolean hasNext() {
        return myInternalIterator.hasNext();
    }

    public D next() {
        return myAction.transform(myInternalIterator.next());
    }

    public void remove() {
        myInternalIterator.remove();
    }
}
