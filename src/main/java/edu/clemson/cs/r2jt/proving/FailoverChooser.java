/**
 * FailoverChooser.java
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

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

import edu.clemson.cs.r2jt.utilities.Mapping;

/**
 * <p>A <code>FailoverChooser</code> is a <code>TransformationChooser</code>
 * that chains two existing <code>TransformationChooser</code>s together in
 * such a way that whenever the first reaches a point where it can suggest no
 * next transformation, the second picks up where the first left off and
 * continues to suggest transformations.</p>
 * 
 * <p>In order to function as expected, this class makes some additional 
 * assumptions about the order in which its methods are called and its returned 
 * <code>Iterator</code>s are used.  Specifically, it assumes that the
 * <code>Iterator</code>s it returns will be exhausted in the reverse order they
 * are provided (i.e., the last <code>Iterator</code> returned will be fully
 * iterated over before any previous one.  Essentially, it assumes the 
 * backtracking, stack-based architecture that the prover currently uses.  
 * Should this change, this class may not behave as expected.</p>
 */
public class FailoverChooser implements TransformationChooser {

    //A key for use with ProofData
    private static final Object FAILED_AT_STEP = new Object();
    private final Mapping<ProofPathSuggestion, ProofPathSuggestion> myNoteAdder;
    private final TransformationChooser myFirstChooser, mySecondChooser;

    public FailoverChooser(TransformationChooser first,
            TransformationChooser second, String failoverNote) {

        myFirstChooser = first;
        mySecondChooser = second;
        myNoteAdder = new NoteAddingMapping(failoverNote);
    }

    public FailoverChooser(TransformationChooser first,
            TransformationChooser second) {

        myFirstChooser = first;
        mySecondChooser = second;
        myNoteAdder = new DummyMapping();
    }

    @Override
    public void preoptimizeForVC(VC vc) {
        myFirstChooser.preoptimizeForVC(vc);
        mySecondChooser.preoptimizeForVC(vc);
    }

    @Override
    public Iterator<ProofPathSuggestion> suggestTransformations(VC vc,
            int curLength, Metrics metrics, ProofData proofData) {

        Iterator<ProofPathSuggestion> retval = null;

        if (proofData.attributeDefined(this, FAILED_AT_STEP)) {
            int failoverStep =
                    (Integer) proofData.getAttribute(this, FAILED_AT_STEP);

            retval =
                    mySecondChooser.suggestTransformations(vc, curLength
                            - failoverStep, metrics, proofData);
        }
        else {
            retval =
                    new FailoverIterator(this, myFirstChooser
                            .suggestTransformations(vc, curLength, metrics,
                                    proofData), vc, curLength, metrics,
                            proofData);
        }

        return retval;
    }

    /**
     * <p>The <code>FailoverIterator</code> wraps an iterator returned by the
     * first transformation chooser, lazily producing an iterator returned by
     * the second only if the first produces zero transformed VC (either
     * because it returns the empty iterator, or each of the transformations
     * it produces, when applied to the VC in question, return empty iterators.
     * </p>
     */
    private class FailoverIterator implements Iterator<ProofPathSuggestion> {

        private final FailoverChooser myParent;
        private final Iterator<ProofPathSuggestion> myFirstIterator;
        private CountingVCTransformer myCurrentTransformer;
        private int myFirstReturnedCount = 0;
        private Iterator<ProofPathSuggestion> mySecondIterator;
        private final VC myOriginalVC;
        private final Metrics myOriginalMetrics;
        private final ProofData myOriginalProofData;
        private final int myOriginalProofLength;
        private ProofPathSuggestion myNextSuggestion;

        public FailoverIterator(FailoverChooser parent,
                Iterator<ProofPathSuggestion> firstIterator, VC originalVC,
                int proofLength, Metrics metrics, ProofData data) {

            myParent = parent;
            myFirstIterator = firstIterator;
            myOriginalVC = originalVC;
            myOriginalMetrics = metrics;
            myOriginalProofData = data;
            myOriginalProofLength = proofLength;
        }

        @Override
        public boolean hasNext() {

            try {
                myNextSuggestion = prepareNext();
            }
            catch (NoSuchElementException e) {
                myNextSuggestion = null;
            }

            return (myNextSuggestion != null);
        }

        private ProofPathSuggestion prepareNext() throws NoSuchElementException {

            ProofPathSuggestion retval;

            //Because we know that iterators are exhausted in the opposite order
            //they are obtained (see parent class comments), we know we're
            //completely done with the last-returned transformer, so get the
            //final count of VCs it transformed and add it to our total
            if (myCurrentTransformer != null) {
                myFirstReturnedCount +=
                        myCurrentTransformer.getVCTransformationCount();
            }

            if (myFirstIterator.hasNext()) {
                retval = myFirstIterator.next();

                myCurrentTransformer = new CountingVCTransformer(retval.step);

                retval =
                        new ProofPathSuggestion(myCurrentTransformer,
                                retval.data);
            }
            else {
                if (myFirstReturnedCount > 0) {
                    throw new NoSuchElementException();
                }
                else {
                    myCurrentTransformer = null;

                    if (mySecondIterator == null) {

                        //System.out.println(myOriginalVC);
                        ProofData newProofData =
                                myOriginalProofData.putAttribute(myParent,
                                        FAILED_AT_STEP,
                                        (Integer) myOriginalProofLength);

                        mySecondIterator =
                                new LazyMappingIterator<ProofPathSuggestion, ProofPathSuggestion>(
                                        mySecondChooser
                                                .suggestTransformations(
                                                        myOriginalVC, 0,
                                                        myOriginalMetrics,
                                                        newProofData),
                                        myNoteAdder);
                    }

                    if (mySecondIterator.hasNext()) {
                        retval = mySecondIterator.next();
                    }
                    else {
                        throw new NoSuchElementException();
                    }
                }
            }

            return retval;
        }

        @Override
        public ProofPathSuggestion next() {
            if (myNextSuggestion == null) {
                throw new NoSuchElementException();
            }

            return myNextSuggestion;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * <p>A <code>CountingTransformer</code> wraps an existing
     * <code>VCTransformer</code> to count the number of <code>VC</code>
     * transformations it has been responsible for suggesting so far.</p>
     */
    private class CountingVCTransformer implements VCTransformer {

        private final VCTransformer mySource;
        private Deque<CountingIterator<?>> myIterators =
                new LinkedList<CountingIterator<?>>();
        private int myFinishedIteratorCountTotal = 0;
        private CountingIterator<VC> myLastIterator;

        public CountingVCTransformer(VCTransformer s) {
            mySource = s;
        }

        public int getVCTransformationCount() {
            return activeIteratorCount() + myFinishedIteratorCountTotal;
        }

        @Override
        public Iterator<VC> transform(VC original) {

            pruneIterators();

            myLastIterator =
                    new CountingIterator<VC>(mySource.transform(original));

            myIterators.add(myLastIterator);

            return myLastIterator;
        }

        private void pruneIterators() {
            while (!myIterators.isEmpty() && !myIterators.peek().hasNext()) {
                popIterator();
            }
        }

        private int activeIteratorCount() {
            int runningTotal = 0;

            for (CountingIterator<?> i : myIterators) {
                runningTotal += i.myReturnedCount;
            }

            return runningTotal;
        }

        private void popIterator() {
            myFinishedIteratorCountTotal +=
                    myIterators.peek().getReturnedCount();
            myIterators.pop();
        }

        @Override
        public String toString() {
            return mySource.toString();
        }

        @Override
        public Antecedent getPattern() {
            return mySource.getPattern();
        }

        @Override
        public Consequent getReplacementTemplate() {
            return mySource.getReplacementTemplate();
        }

        @Override
        public boolean introducesQuantifiedVariables() {
            return mySource.introducesQuantifiedVariables();
        }
    }

    private class CountingIterator<T> implements Iterator<T> {

        private int myReturnedCount = 0;
        private final Iterator<T> mySource;

        public CountingIterator(Iterator<T> i) {
            mySource = i;
        }

        @Override
        public boolean hasNext() {
            return mySource.hasNext();
        }

        @Override
        public T next() {
            //Note that if there are no more elements, mySource.next() will
            //throw a NoSuchElementException and we won't increment our counter
            T retval = mySource.next();

            myReturnedCount++;

            return retval;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        public int getReturnedCount() {
            return myReturnedCount;
        }
    }

    private class NoteAddingMapping
            implements
                Mapping<ProofPathSuggestion, ProofPathSuggestion> {

        private final String myNote;

        public NoteAddingMapping(String note) {
            myNote = note;
        }

        @Override
        public ProofPathSuggestion map(ProofPathSuggestion i) {
            return new ProofPathSuggestion(i.step, i.data, myNote,
                    "Failed over from " + myFirstChooser + " to "
                            + mySecondChooser + " with VC:", true);
        }
    }

    private class DummyMapping
            implements
                Mapping<ProofPathSuggestion, ProofPathSuggestion> {

        @Override
        public ProofPathSuggestion map(ProofPathSuggestion i) {
            return new ProofPathSuggestion(i.step, i.data, null,
                    "Failed over from " + myFirstChooser + " to "
                            + mySecondChooser + " with VC:", true);
        }
    }
}
