/*
 * Copyright 2005 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.common.geometry;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * An S2CellUnion is a region consisting of cells of various sizes. Typically a
 * cell union is used to approximate some other shape. There is a trade off
 * between the accuracy of the approximation and how many cells are used. Unlike
 * polygons, cells have a fixed hierarchical structure. This makes them more
 * suitable for optimizations based on preprocessing.
 */
public strictfp class S2CellUnion implements S2Region, Iterable<S2CellId> {

    /**
     * The CellIds that form the Union
     */
    private ArrayList<S2CellId> cellIds = new ArrayList<>();

    public S2CellUnion() {
    }

    /**
     * Creates a cell union with the given {@link S2CellId}(s) and then calls {@link #normalize()}.
     *
     * @param cellIds cell ids that form the union
     */
    public void initFromCellIds(ArrayList<S2CellId> cellIds) {
        initRawCellIds(cellIds);
        normalize();
    }

    /**
     * Creates a cell union with the given 64-bit cells id values, and then calls {@link #normalize()}.
     *
     * @param cellIds 64-bit cell id values that form the union
     */
    public void initFromIds(ArrayList<Long> cellIds) {
        initRawIds(cellIds);
        normalize();
    }

    /**
     * Populates a cell union with the given {@link S2CellId}(s) and then calls {@link #normalize()}.
     * <p>
     * Takes ownership of the input {@link ArrayList} data without copying and clears it.
     *
     * @param cellIds cell ids that form the union
     */
    public void initSwap(ArrayList<S2CellId> cellIds) {
        initRawSwap(cellIds);
        normalize();
    }

    /**
     * Like {@link #initFromCellIds(ArrayList)}, but does not call {@link #normalize()}.
     * <p>
     * The cell union *must* be normalized before doing any calculations with it,
     * so it is the caller's responsibility to make sure that the input is normalized.
     * This method is useful when converting cell unions to another representation and back.
     *
     * @param cellIds cell ids that form the union
     */
    public void initRawCellIds(ArrayList<S2CellId> cellIds) {
        this.cellIds = cellIds;
    }

    /**
     * Like {@link #initFromIds(ArrayList)}, but does not call {@link #normalize()}.
     * <p>
     * The cell union *must* be normalized before doing any calculations with it,
     * so it is the caller's responsibility to make sure that the input is normalized.
     * This method is useful when converting cell unions to another representation and back.
     *
     * @param cellIds 64-bit cell id values that form the union
     */
    public void initRawIds(ArrayList<Long> cellIds) {
        int size = cellIds.size();
        this.cellIds = new ArrayList<>(size);
        for (Long id : cellIds) {
            this.cellIds.add(new S2CellId(id));
        }
    }

    /**
     * Like {@link #initSwap(ArrayList)}, but does not call {@link #normalize()}.
     * <p>
     * The cell union *must* be normalized before doing any calculations with it,
     * so it is the caller's responsibility to make sure that the input is normalized.
     * This method is useful when converting cell unions to another representation and back.
     *
     * @param cellIds cell ids that form the union
     */
    public void initRawSwap(ArrayList<S2CellId> cellIds) {
        this.cellIds = new ArrayList<>(cellIds);
        cellIds.clear();
    }

    public int size() {
        return cellIds.size();
    }

    /**
     * Convenience methods for accessing the individual cell ids.
     */
    public S2CellId cellId(int i) {
        return cellIds.get(i);
    }

    /**
     * Enable iteration over the union's cells.
     */
    public Iterator<S2CellId> iterator() {
        return cellIds.iterator();
    }

    /**
     * Direct access to the underlying vector for iteration.
     */
    public ArrayList<S2CellId> cellIds() {
        return cellIds;
    }

    /**
     * Replaces "output" with an expanded version of the cell union where any
     * cells whose level is less than "minLevel" or where (level - minLevel) is
     * not a multiple of "levelMod" are replaced by their children, until either
     * both of these conditions are satisfied or the maximum level is reached.
     * <p>
     * This method allows a covering generated by {@link S2RegionCoverer} using
     * {@link S2RegionCoverer#minLevel()} or {@link S2RegionCoverer#levelMod()}
     * constraints to be stored as a normalized cell union (which allows various
     * geometric computations to be done) and then converted back to the original
     * list of cell ids that satisfies the desired constraints.
     *
     * @param minLevel minimum level
     * @param levelMod level modulo
     * @param output   S2CellId(s) from denormalizing the union
     */
    public void denormalize(int minLevel, int levelMod, ArrayList<S2CellId> output) {
        // assert (minLevel >= 0 && minLevel <= S2CellId.MAX_LEVEL);
        // assert (levelMod >= 1 && levelMod <= 3);
        Preconditions.checkArgument(minLevel >= 0 && minLevel <= S2CellId.MAX_LEVEL);
        Preconditions.checkArgument(levelMod >= 1 && levelMod <= 3);

        output.clear();
        output.ensureCapacity(size());
        for (S2CellId id : this) {
            int level = id.level();
            int newLevel = Math.max(minLevel, level);
            if (levelMod > 1) {
                // Round up so that (newLevel - minLevel) is a multiple of levelMod...
                // (Note that S2CellId.MAX_LEVEL is a multiple of 1, 2, and 3.)
                newLevel += (S2CellId.MAX_LEVEL - (newLevel - minLevel)) % levelMod;
                newLevel = Math.min(S2CellId.MAX_LEVEL, newLevel);
            }
            if (newLevel == level) {
                output.add(id);
            } else {
                S2CellId end = id.childEnd(newLevel);
                for (id = id.childBegin(newLevel); !id.equals(end); id = id.next()) {
                    output.add(id);
                }
            }
        }
    }

    /**
     * If there are more than "excess" elements of the cellIds() array list that are
     * allocated but unused, reallocate the array to eliminate the excess space.
     * This reduces memory usage when many cell unions need to be held
     * in memory at once.
     */
    public void pack() {
        cellIds.trimToSize();
    }

    /**
     * Return true if the cell union contains the given cell id. Containment is
     * defined with respect to regions, e.g. a cell contains its 4 children. This
     * is a fast operation (logarithmic in the size of the cell union).
     */
    public boolean contains(S2CellId id) {
        // This function requires that normalize() has been called first.
        //
        // This is an exact test. Each cell occupies a linear span of the S2
        // space-filling curve, and the cell id is simply the position at the center
        // of this span. The cell union ids are sorted in increasing order along
        // the space-filling curve. So we simply find the pair of cell ids that
        // surround the given cell id (using binary search). There is containment
        // if and only if one of these two cell ids contains this cell.

        int pos = Collections.binarySearch(cellIds, id);
        if (pos < 0) {
            pos = -pos - 1;
        }
        if (pos < cellIds.size() && cellIds.get(pos).rangeMin().lessOrEquals(id)) {
            return true;
        }
        return pos != 0 && cellIds.get(pos - 1).rangeMax().greaterOrEquals(id);
    }

    /**
     * Return true if the cell union intersects the given cell id. This is a fast
     * operation (logarithmic in the size of the cell union).
     */
    public boolean intersects(S2CellId id) {
        // This function requires that normalize() has been called first.
        // This is an exact test; see the comments for contains() above.
        int pos = Collections.binarySearch(cellIds, id);

        if (pos < 0) {
            pos = -pos - 1;
        }

        if (pos < cellIds.size() && cellIds.get(pos).rangeMin().lessOrEquals(id.rangeMax())) {
            return true;
        }
        return pos != 0 && cellIds.get(pos - 1).rangeMax().greaterOrEquals(id.rangeMin());
    }

    /**
     * Return true if the cell union contains the given other cell union. Containment is
     * defined with respect to regions, e.g. a cell contains its 4 children. This
     * is a fast operation (logarithmic in the size of the cell union).
     */
    public boolean contains(S2CellUnion that) {
        // TODO(kirilll?): A divide-and-conquer or alternating-skip-search approach may be significantly faster in both the average and worst case.
        for (S2CellId id : that) {
            if (!this.contains(id)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Return true if the cell union contains the given cell. Containment is
     * defined with respect to regions, e.g. a cell contains its 4 children. This
     * is a fast operation (logarithmic in the size of the cell union).
     */
    public boolean contains(S2Cell cell) {
        return contains(cell.id());
    }

    /**
     * Return true if this cell union contain/intersects the given other cell union.
     */
    public boolean intersects(S2CellUnion union) {
        // TODO(kirilll?): A divide-and-conquer or alternating-skip-search approach may be significantly faster in both the average and worst case.
        for (S2CellId id : union) {
            if (intersects(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Create a new cell union that is the union of the 2 inputs.
     */
    @VisibleForTesting
    void getUnion(S2CellUnion x, S2CellUnion y) {
        // assert (x != this && y != this);
        Preconditions.checkArgument(x != this && y != this);

        cellIds.clear();
        cellIds.ensureCapacity(x.size() + y.size());
        cellIds.addAll(x.cellIds);
        cellIds.addAll(y.cellIds);
        normalize();
    }

    /**
     * Specialized version of getIntersection() that gets the intersection of a
     * cell union with the given cell id. This can be useful for "splitting" a
     * cell union into chunks.
     */
    public void getIntersection(S2CellUnion x, S2CellId id) {
        // assert (x != this);
        Preconditions.checkArgument(x != this);

        cellIds.clear();
        if (x.contains(id)) {
            cellIds.add(id);
        } else {
            int pos = Collections.binarySearch(x.cellIds, id.rangeMin());
            if (pos < 0) {
                pos = -pos - 1;
            }
            S2CellId idmax = id.rangeMax();
            int size = x.cellIds.size();
            while (pos < size && x.cellIds.get(pos).lessOrEquals(idmax)) {
                cellIds.add(x.cellIds.get(pos++));
            }
        }
    }

    /**
     * Initialize this cell union to the union or intersection of the two given
     * cell unions. Requires: x != this and y != this.
     */
    public void getIntersection(S2CellUnion x, S2CellUnion y) {
        // assert (x != this && y != this);
        Preconditions.checkArgument(x != this && y != this);

        // This is a fairly efficient calculation that uses binary search to skip
        // over sections of both input vectors. It takes constant time if all the
        // cells of "x" come before or after all the cells of "y" in S2CellId order.

        cellIds.clear();

        int i = 0;
        int j = 0;

        while (i < x.cellIds.size() && j < y.cellIds.size()) {
            S2CellId imin = x.cellId(i).rangeMin();
            S2CellId jmin = y.cellId(j).rangeMin();
            if (imin.greaterThan(jmin)) {
                // Either j->contains(*i) or the two cells are disjoint.
                if (x.cellId(i).lessOrEquals(y.cellId(j).rangeMax())) {
                    cellIds.add(x.cellId(i++));
                } else {
                    // Advance "j" to the first cell possibly contained by *i.
                    j = indexedBinarySearch(y.cellIds, imin, j + 1);
                    // The previous cell *(j-1) may now contain *i.
                    if (x.cellId(i).lessOrEquals(y.cellId(j - 1).rangeMax())) {
                        --j;
                    }
                }
            } else if (jmin.greaterThan(imin)) {
                // Identical to the code above with "i" and "j" reversed.
                if (y.cellId(j).lessOrEquals(x.cellId(i).rangeMax())) {
                    cellIds.add(y.cellId(j++));
                } else {
                    i = indexedBinarySearch(x.cellIds, jmin, i + 1);
                    if (y.cellId(j).lessOrEquals(x.cellId(i - 1).rangeMax())) {
                        --i;
                    }
                }
            } else {
                // "i" and "j" have the same range_min(), so one contains the other.
                if (x.cellId(i).lessThan(y.cellId(j))) {
                    cellIds.add(x.cellId(i++));
                } else {
                    cellIds.add(y.cellId(j++));
                }
            }
        }
        // The output is generated in sorted order, and there should not be any
        // cells that can be merged (provided that both inputs were normalized).
        // assert (!normalize());
    }

    /**
     * Just as normal binary search, except that it allows specifying the starting value for the lower bound.
     *
     * @return The position of the searched element in the list (if found),
     * or the position where the element could be inserted without violating the order.
     */
    private int indexedBinarySearch(List<S2CellId> l, S2CellId key, int low) {
        int high = l.size() - 1;

        while (low <= high) {
            int mid = (low + high) >> 1;
            S2CellId midVal = l.get(mid);
            int cmp = midVal.compareTo(key);

            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return low; // key not found
    }

    /**
     * Expands the cell union such that it contains all cells of the given level
     * that are adjacent to any cell of the original union. Two cells are defined
     * as adjacent if their boundaries have any points in common, i.e. most cells
     * have 8 adjacent cells (not counting the cell itself).
     * <p>
     * Note that the size of the output is exponential in "level". For example,
     * if level == 20 and the input has a cell at level 10, there will be on the
     * order of 4000 adjacent cells in the output. For most applications the
     * {@link #expand(S1Angle, int)} method below is easier to use.
     */
    public void expand(int level) {
        ArrayList<S2CellId> output = new ArrayList<S2CellId>();
        long levelLsb = S2CellId.lowestOnBitForLevel(level);
        int i = size() - 1;
        do {
            S2CellId id = cellId(i);
            if (id.lowestOnBit() < levelLsb) {
                id = id.parent(level);
                // Optimization: skip over any cells contained by this one. This is
                // especially important when very small regions are being expanded.
                while (i > 0 && id.contains(cellId(i - 1))) {
                    --i;
                }
            }
            output.add(id);
            id.getAllNeighbors(level, output);
        } while (--i >= 0);
        initSwap(output);
    }

    /**
     * Expand the cell union such that it contains all points whose distance to
     * the cell union is at most minRadius, but do not use cells that are more
     * than maxLevelDiff levels higher than the largest cell in the input. The
     * second parameter controls the trade off between accuracy and output size
     * when a large region is being expanded by a small amount (e.g. expanding
     * Canada by 1km).
     * <p>
     * For example, if maxLevelDiff == 4, the region will always be expanded by
     * approximately 1/16 the width of its largest cell. Note that in the worst
     * case, the number of cells in the output can be up to 4 * (1 + 2 **
     * maxLevelDiff) times larger than the number of cells in the input.
     */
    public void expand(S1Angle minRadius, int maxLevelDiff) {
        int minLevel = S2CellId.MAX_LEVEL;
        for (S2CellId id : this) {
            minLevel = Math.min(minLevel, id.level());
        }
        // Find the maximum level such that all cells are at least "min_radius" wide.
        int radiusLevel = S2Projections.MIN_WIDTH.getMaxLevel(minRadius.radians());
        if (radiusLevel == 0 && minRadius.radians() > S2Projections.MIN_WIDTH.getValue(0)) {
            // The requested expansion is greater than the width of a face cell.
            // The easiest way to handle this is to expand twice.
            expand(0);
        }
        expand(Math.min(minLevel + maxLevelDiff, radiusLevel));
    }

    @Override
    public S2Region clone() {
        S2CellUnion copy = new S2CellUnion();
        copy.initRawCellIds(Lists.newArrayList(cellIds));
        return copy;
    }

    public S2Cap getCapBound() {
        // Compute the approximate centroid of the region. This won't produce the
        // bounding cap of minimal area, but it should be close enough.
        if (cellIds.isEmpty()) {
            return S2Cap.empty();
        }
        S2Point centroid = new S2Point(0, 0, 0);
        for (S2CellId id : this) {
            double area = S2Cell.averageArea(id.level());
            centroid = S2Point.add(centroid, S2Point.mul(id.toPoint(), area));
        }
        if (centroid.equals(new S2Point(0, 0, 0))) {
            centroid = new S2Point(1, 0, 0);
        } else {
            centroid = S2Point.normalize(centroid);
        }

        // Use the centroid as the cap axis, and expand the cap angle so that it
        // contains the bounding caps of all the individual cells. Note that it is
        // *not* sufficient to just bound all the cell vertices because the bounding
        // cap may be concave (i.e. cover more than one hemisphere).
        S2Cap cap = S2Cap.fromAxisHeight(centroid, 0);
        for (S2CellId id : this) {
            cap = cap.addCap(new S2Cell(id).getCapBound());
        }
        return cap;
    }

    public S2LatLngRect getRectBound() {
        S2LatLngRect bound = S2LatLngRect.empty();
        for (S2CellId id : this) {
            bound = bound.union(new S2Cell(id).getRectBound());
        }
        return bound;
    }

    /**
     * Return true if the cell union (may) intersect the given cell id. This is a fast
     * operation (logarithmic in the size of the cell union).
     */
    public boolean mayIntersect(S2Cell cell) {
        return intersects(cell.id());
    }

    /**
     * The point 'p' does not need to be normalized.
     * This is a fast operation (logarithmic in the size of the cell union).
     */
    public boolean contains(S2Point p) {
        return contains(S2CellId.fromPoint(p));

    }

    /**
     * The number of leaf cells covered by the union. This will be no more than 6*2^60 for the whole sphere.
     *
     * @return the number of leaf cells covered by the union
     */
    public long leafCellsCovered() {
        long numLeaves = 0;
        for (S2CellId cellId : cellIds) {
            int invertedLevel = S2CellId.MAX_LEVEL - cellId.level();
            numLeaves += (1L << (invertedLevel << 1));
        }
        return numLeaves;
    }

    /**
     * Approximate this cell union's area by summing the average area of each contained
     * cell's average area, using {@link S2Cell#averageArea()}. This is equivalent to
     * the number of leaves covered, multiplied by the average area of a leaf. Note that
     * {@link S2Cell#averageArea()} does not take into account distortion of cell, and
     * thus may be off by up to a factor of 1.7. NOTE: Since this is proportional to
     * {@link #leafCellsCovered()}, it is always better to use the other function
     * if all you care about is the relative average area between objects.
     *
     * @return the sum of the average area of each contained cell's average area
     */
    public double averageBasedArea() {
        return S2Cell.averageArea(S2CellId.MAX_LEVEL) * leafCellsCovered();
    }

    /**
     * Calculates this cell union's area by summing the approximate area for each contained cell,
     * using {@link S2Cell#approxArea()}.
     *
     * @return approximate area of the cell union
     */
    public double approxArea() {
        double area = 0;
        for (S2CellId cellId : cellIds) {
            area += new S2Cell(cellId).approxArea();
        }
        return area;
    }

    /**
     * Calculates this cell union's area by summing the exact area for each contained cell,
     * using the {@link S2Cell#exactArea()}.
     *
     * @return the exact area of the cell union
     */
    public double exactArea() {
        double area = 0;
        for (S2CellId cellId : cellIds) {
            area += new S2Cell(cellId).exactArea();
        }
        return area;
    }

    /**
     * Return true if two cell unions are identical.
     */
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof S2CellUnion)) {
            return false;
        }
        S2CellUnion union = (S2CellUnion) that;
        return this.cellIds.equals(union.cellIds);
    }

    @Override
    public int hashCode() {
        int value = 17;
        for (S2CellId id : this) {
            value = 37 * value + id.hashCode();
        }
        return value;
    }

    /**
     * Normalizes the cell union by discarding cells that are contained by other
     * cells, replacing groups of 4 child cells by their parent cell whenever possible,
     * and sorting all the cell ids in increasing order. Returns true if the number of cells was reduced.
     * <p>
     * This method *must* be called before doing any calculations on the cell union,
     * such as {@link #intersects(S2CellId)} or {@link #contains(S2Cell)}.
     *
     * @return true if the normalize operation had any effect on union, false if the union was already normalized
     */
    public boolean normalize() {
        // Optimize the representation by looking for cases where all subcells of a parent cell are present.

        ArrayList<S2CellId> output = new ArrayList<>(cellIds.size());
        output.ensureCapacity(cellIds.size());
        Collections.sort(cellIds);

        for (S2CellId id : this) {
            int size = output.size();
            // Check whether this cell is contained by the previous cell.
            if (!output.isEmpty() && output.get(size - 1).contains(id)) {
                continue;
            }

            // Discard any previous cells contained by this cell.
            while (!output.isEmpty() && id.contains(output.get(output.size() - 1))) {
                output.remove(output.size() - 1);
            }

            // Check whether the last 3 elements of "output" plus "id" can be collapsed into a single parent cell.
            while (output.size() >= 3) {
                size = output.size();
                // A necessary (but not sufficient) condition is that the XOR of the
                // four cells must be zero. This is also very fast to test.
                if ((output.get(size - 3).id() ^ output.get(size - 2).id() ^ output.get(size - 1).id()) != id.id()) {
                    break;
                }

                // Now we do a slightly more expensive but exact test. First, compute a
                // mask that blocks out the two bits that encode the child position of
                // "id" with respect to its parent, then check that the other three
                // children all agree with "mask.
                long mask = id.lowestOnBit() << 1;
                mask = ~(mask + (mask << 1));
                long idMasked = (id.id() & mask);
                if ((output.get(size - 3).id() & mask) != idMasked
                        || (output.get(size - 2).id() & mask) != idMasked
                        || (output.get(size - 1).id() & mask) != idMasked || id.isFace()) {
                    break;
                }

                // Replace four children by their parent cell.
                output.remove(size - 1);
                output.remove(size - 2);
                output.remove(size - 3);
                id = id.parent();
            }
            output.add(id);
        }
        if (output.size() < size()) {
            initRawSwap(output);
            return true;
        }
        return false;
    }
}
