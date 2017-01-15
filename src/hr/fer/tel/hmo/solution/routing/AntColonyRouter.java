package hr.fer.tel.hmo.solution.routing;

import hr.fer.tel.hmo.network.Topology;
import hr.fer.tel.hmo.solution.proxies.LinkProxy;
import hr.fer.tel.hmo.solution.proxies.NodeProxy;
import hr.fer.tel.hmo.util.Matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Find route using ant colony optimization.
 */
public class AntColonyRouter extends SequentialRouter {

	private static final int NUM_ANTS = 4;
	private static final int ITERATIONS = 100;

	private static final double ALPHA = 1.0;
	private static final double BETA = 1.0;
	private static final double RHO = 0.1;

	private Matrix<Integer, Integer, Double> pheromones;

	AntColonyRouter(Topology topology) {
		super(topology);
	}

	@Override
	protected void initialize() {
		super.initialize();

		pheromones = new Matrix<>();
		for (Map.Entry<NodeProxy, List<LinkProxy>> e : neighbors.entrySet()) {
			NodeProxy np = e.getKey();
			List<LinkProxy> lps = e.getValue();

			double tau = 1. / lps.size();
			lps.forEach(lp -> pheromones.put(np.node.getIndex(), lp.to.node.getIndex(), tau));
		}

	}

	@Override
	protected List<Integer> path(NodeProxy from, NodeProxy end, double delay, double bandwidth) {

		// Ant colony route finding

		PowerRoute best = new PowerRoute();

		Map<NodeProxy, List<LinkProxy>> valids = validNeighbors(delay, bandwidth);

		int iteration = 0;
		while (iteration++ < ITERATIONS) {

			PowerRoute currentBest = new PowerRoute();


			for (int __ = 0; __ < NUM_ANTS; __++) {
				currentBest = currentBest.better(ant(from, end, valids));
			}

			if (!currentBest.exists()) {
				return null;
			}

			// TODO delta = f(power)
			double delta = 1. / currentBest.power;

			// EVAPORATION
			pheromones.map(tau -> tau * (1 - RHO));

			// REINFORCEMENT
			int prev = currentBest.route.get(0);
			for (int i = 1, N = currentBest.route.size(); i < N; i++) {
				int curr = currentBest.route.get(i);
				pheromones.compute(prev, curr, tau -> tau + delta);
				prev = curr;
			}

			best = best.better(currentBest);
		}

		if (best.exists()) {
			// UPDATE links and nodes, use them
			int prev = best.route.get(0);
			nodes[prev].used = true;
			for (int i = 1, N = best.route.size(); i < N; i++) {
				int curr = best.route.get(i);
				for (LinkProxy lp : neighbors.get(nodes[prev])){
					if (lp.to.node.getIndex() == curr){
						lp.used = true;
					}
				}
				nodes[curr].used = true;
				prev = curr;
			}
		}

		return best.route;
	}

	private PowerRoute ant(NodeProxy from, NodeProxy to, Map<NodeProxy, List<LinkProxy>> neighbors) {

		// TODO

		List<Integer> route = new ArrayList<>();
		double power = 0.0;

		while (!from.equals(to)) {

		}

		route.add(to.node.getIndex());

		return new PowerRoute(power, route);
	}

	/**
	 * @param delay     delay
	 * @param bandwidth bandwidth
	 * @return map containing only valid links
	 */
	private Map<NodeProxy, List<LinkProxy>> validNeighbors(double delay, double bandwidth) {

		final Map<NodeProxy, List<LinkProxy>> valid = new HashMap<>();

		for (Map.Entry<NodeProxy, List<LinkProxy>> e : neighbors.entrySet()) {

			List<LinkProxy> lps = e.getValue().stream()
					.filter(lp -> lp.validParams(delay, bandwidth))
					.collect(Collectors.toList());

			if (!lps.isEmpty()) {
				valid.put(e.getKey(), lps);
			}
		}

		return valid;
	}

	private static class PowerRoute {
		double power;
		List<Integer> route;

		PowerRoute() {
			this(Double.MAX_VALUE, null);
		}

		PowerRoute(double power, List<Integer> route) {
			this.power = power;
			this.route = route;
		}

		/**
		 * @return if it has a valid route
		 */
		boolean exists() {
			return route != null;
		}

		/**
		 * @param other other route
		 * @return this or other, which ever is better
		 */
		PowerRoute better(PowerRoute other) {
			return other == null || power <= other.power ? this : other;
		}
	}

}
