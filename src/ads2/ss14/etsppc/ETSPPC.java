package ads2.ss14.etsppc;

import java.util.*;

/**
 * Klasse zum Berechnen der Tour mittels Branch-and-Bound.
 * Hier sollen Sie Ihre L&ouml;sung implementieren.
 */
public class ETSPPC extends AbstractETSPPC {

	private final ETSPPCInstance instance;
	private final Map<Integer, Location> allLocationsMap;
	private final Collection<Location> allLocationsList;
	private final int allLocationsSize;
	private final List<PrecedenceConstraint> constraints;

	private double upperBound = Double.MAX_VALUE;

//	private final double[][] distanceMatrix;

	private double minimalDistance = Double.POSITIVE_INFINITY;

	public ETSPPC(ETSPPCInstance instance) {
		this.instance = instance;
		this.allLocationsMap = instance.getAllLocations();
		this.allLocationsList = instance.getAllLocations().values();
		this.allLocationsSize = allLocationsMap.size();
		this.constraints = instance.getConstraints();

//		distanceMatrix = new double[allLocationsSize][allLocationsSize];
		for (Location l1: allLocationsList) {
			int l1id = l1.getCityId() - 1;
			for (Location l2: allLocationsList) {
				int l2id = l2.getCityId() - 1;

				if (l1id == l2id) {
//					distanceMatrix[l1id][l2id] = Double.POSITIVE_INFINITY;
				} else {
					double distance = l1.distanceTo(l2);
//					distanceMatrix[l1id][l2id] = distance;
					if (distance < minimalDistance) {
						minimalDistance = distance;
					}
				}
			}
		}
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
		if (visited.size() == allLocationsSize) {
			double value = Main.calcObjectiveValue(visited);
			setSolution(value, visited);
			upperBound = value;
			return;
		}

		ArrayList<Location> remainingLocations = new ArrayList<Location>(allLocationsSize - visited.size());

		// Filter already visited locations
		for (Location l: allLocationsList) {
			if (! visited.contains(l)) {
				remainingLocations.add(l);
			}
		}

		// Prune locations that are not reachable because of constraints
		for (PrecedenceConstraint pc : constraints) {
			if (! visited.contains(allLocationsMap.get(pc.getFirst()))) {
				remainingLocations.remove(allLocationsMap.get(pc.getSecond()));
			}
		}

		// Choose the Nearest Neighbour first
		final Location lastLocation;
		if (visited.size() == 0) {
			lastLocation = null;
		} else {
			lastLocation = visited.get(visited.size() - 1);
		}
		Collections.sort(remainingLocations, new Comparator<Location>() {
			@Override
			public int compare(Location l1, Location l2) {
				if (lastLocation == null) return 0;

				Double l1Cost = lastLocation.distanceTo(l1);
				Double l2Cost = lastLocation.distanceTo(l2);

				return l1Cost.compareTo(l2Cost);
			}
		});

		for (Location nextLocation: remainingLocations) {
			ArrayList<Location> newVisited = new ArrayList<Location>(visited.size() + 1);
			newVisited.addAll(visited);
			newVisited.add(nextLocation);

			double lowerBound = Main.calcObjectiveValue(newVisited);
			lowerBound += (allLocationsSize - newVisited.size()) * minimalDistance;
			if (lowerBound < upperBound) {
				search(newVisited);
			}
		}
	}
}
