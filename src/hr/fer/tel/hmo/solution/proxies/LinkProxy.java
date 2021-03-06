package hr.fer.tel.hmo.solution.proxies;


import hr.fer.tel.hmo.network.Link;

import java.util.Comparator;
import java.util.HashSet;

/**
 * Proxy class for Link
 */
public class LinkProxy implements Comparable<LinkProxy> {

	public final NodeProxy to;
	public final double delay;
	public double bandwidth;
	private final double power;
	public boolean used;

	public LinkProxy(NodeProxy to, Link link) {
		this.to = to;
		delay = link.getDelay();
		bandwidth = link.getBandwidth();
		power = link.getPowerConsumption();
		used = false;
	}

	/**
	 * Check if delay and bandwidth constraints are satisfied with this link
	 *
	 * @param delay     delay
	 * @param bandwidth bandwidth
	 * @return true if they are satisfied
	 */
	public boolean validParams(double delay, double bandwidth) {
		return this.delay <= delay && bandwidth <= this.bandwidth;
	}

	/**
	 * How much will power rise if we choose this link
	 *
	 * @return power up
	 */
	private double powerUp() {
		double power = 0.0;
		if (!used) {
			power += this.power;
		}
		if (!to.used) {
			power += to.node.getPowerConsumption();
		}
		return power;
	}

	public double powerUp(HashSet<NodeProxy> nps, HashSet<LinkProxy> lps) {
		double power = 0.0;
		if (!used && !lps.contains(this)) {
			power += this.power;
		}
		if (!to.used && !nps.contains(to)) {
			power += to.node.getPowerConsumption();
		}
		return power;
	}

	@Override
	public int compareTo(LinkProxy o) {
		return Double.compare(powerUp(), o.powerUp());
	}

	public static class LinkComp implements Comparator<LinkProxy> {

		private final NodeProxy goalNode;

		public LinkComp(NodeProxy goalNode) {
			this.goalNode = goalNode;
		}

		@Override
		public int compare(LinkProxy x, LinkProxy y) {
			// returns -1 if x is better, 0 if they are same, 1 if y is better

			if (y == null) {
				return -1;
			}
			if (x == null) {
				return 1;
			}

			if (x.to.equals(y.to)) {
				return x.compareTo(y);
			}

			if (goalNode.equals(x.to)) {
				return -1;
			}

			if (goalNode.equals(y.to)) {
				return 1;
			}

			return x.compareTo(y);
		}
	}

}
