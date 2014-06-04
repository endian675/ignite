/* @java.file.header */

/*  _________        _____ __________________        _____
 *  __  ____/___________(_)______  /__  ____/______ ____(_)_______
 *  _  / __  __  ___/__  / _  __  / _  / __  _  __ `/__  / __  __ \
 *  / /_/ /  _  /    _  /  / /_/ /  / /_/ /  / /_/ / _  /  _  / / /
 *  \____/   /_/     /_/   \_,__/   \____/   \__,_/  /_/   /_/ /_/
 */

package org.gridgain.grid.kernal.visor.cmd;

import org.gridgain.grid.*;
import org.gridgain.grid.compute.*;
import org.gridgain.grid.util.lang.*;
import org.jetbrains.annotations.*;

import java.util.*;

/**
 * Base class for Visor tasks intended to query data from a single node.
 *
 * @param <A> Task argument type.
 * @param <R> Task result type.
 */
public abstract class VisorOneNodeTask<A extends VisorOneNodeArg, R> implements GridComputeTask<A, R> {
    /**
     * Create task job.
     *
     * @param arg Task arg.
     * @return New job.
     */
    protected abstract VisorJob<A, R> job(A arg);

    @Nullable @Override public Map<? extends GridComputeJob, GridNode> map(List<GridNode> subgrid,
        @Nullable A arg) throws GridException {
        assert arg != null;

        for (GridNode node : subgrid)
            if (node.id().equals(arg.nodeId()))
                return Collections.singletonMap(job(arg), node);

        throw new GridEmptyProjectionException(
                "Target node to execute Visor job not found in grid [id=" + arg.nodeId() + ", prj=" + subgrid + "]");
    }

    @Override public GridComputeJobResultPolicy result(GridComputeJobResult res,
        List<GridComputeJobResult> rcvd) throws GridException {
        // All Visor tasks should handle exceptions in reduce method.
        return GridComputeJobResultPolicy.WAIT;
    }

    /**
     * Process job result.
     *
     * @param res Result to process.
     * @return Job result.
     */
    protected R reduce(GridComputeJobResult res) throws GridException {
        if (res.getException() == null)
            return res.getData();

        throw res.getException();
    }

    /** {@inheritDoc} */
    @Nullable @Override public R reduce(List<GridComputeJobResult> results) throws GridException {
        assert results.size() == 1;

        return reduce(GridFunc.first(results));
    }
}
