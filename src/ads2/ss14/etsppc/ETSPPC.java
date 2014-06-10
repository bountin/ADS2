package ads2.ss14.etsppc;

import java.util.*;

/**
 * Klasse zum Berechnen der Tour mittels Branch-and-Bound.
 * Hier sollen Sie Ihre L&ouml;sung implementieren.
 */
public class ETSPPC extends AbstractETSPPC {

	ETSPPCInstance instance;

	public ETSPPC(ETSPPCInstance instance) {
		this.instance = instance;
	}

	/**
	 * Diese Methode bekommt vom Framework maximal 30 Sekunden Zeit zur
	 * Verf&uuml;gung gestellt um eine g&uuml;ltige Tour
	 * zu finden.
	 * 
	 * <p>
	 * F&uuml;gen Sie hier Ihre Implementierung des Branch-and-Bound Algorithmus
	 * ein.
	 * </p>
	 */
	@Override
	public void run() {
		search(new ArrayList<Location>());
	}

	private void search(final ArrayList<Location> visited) {
		if (visited.size() == instance.getAllLocations().size()) {
			setSolution(Main.calcObjectiveValue(visited), visited);
			return;
		}

		ArrayList<Location> remainingLocations = new ArrayList<Location>();

		// Filter already visited locations
		for (Location l: instance.getAllLocations().values()) {
			if (! visited.contains(l)) {
				remainingLocations.add(l);
			}
		}

		// Prune locations that are not reachable because of constraints
		for(PrecedenceConstraint pc : instance.getConstraints()) {
			if (! visited.contains(instance.getAllLocations().get(pc.getFirst()))) {
				remainingLocations.remove(instance.getAllLocations().get(pc.getSecond()));
			}
		}

		// Choose the Nearest Neighbour first
		Collections.sort(remainingLocations, new Comparator<Location>() {
			@Override
			public int compare(Location l1, Location l2) {
				if (visited.size() == 0) return 0;

				Double l1Cost = visited.get(visited.size() - 1).distanceTo(l1);
				Double l2Cost = visited.get(visited.size() - 1).distanceTo(l2);

				return l1Cost.compareTo(l2Cost);
			}
		});

		for (Location nextLocation: remainingLocations) {
			ArrayList<Location> newVisited = new ArrayList<Location>(visited.size());
			for (Location l: visited) newVisited.add(l);

			newVisited.add(nextLocation);

			BnBSolution bestSolution = getBestSolution();
			if (bestSolution == null || bestSolution.getUpperBound() > Main.calcObjectiveValue(newVisited)) {
				search(newVisited);
			}
		}
	}
}
