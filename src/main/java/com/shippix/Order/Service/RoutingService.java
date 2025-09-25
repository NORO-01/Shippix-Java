package com.shippix.Order.Service;

import java.util.*;

import org.springframework.stereotype.Service;

import com.shippix.Order.Model.Order;
import com.shippix.Order.Model.Truck;
import com.shippix.Order.Model.Warehouse;
import com.shippix.Order.Repo.OrderRepo;
import com.shippix.Order.Repo.TruckRepo;
import com.shippix.Order.Repo.WarehouseRepo;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoutingService {

    private final OrderRepo orderRepo;
    private final WarehouseRepo warehouseRepo;
    private final TruckRepo truckRepo;

    // ---------------- Existing helper methods ---------------- //

    public List<Order> getOrders(Long warehouseId) {
        return orderRepo.findInWarehouseOrdersByWarehouseSorted(warehouseId);
    }

    public Map<String, List<Order>> splitByDirection(List<Order> orders, double warehouseLat, double warehouseLng) {
        Map<String, List<Order>> groups = new HashMap<>();
        groups.put("NORTH", new ArrayList<>());
        groups.put("SOUTH", new ArrayList<>());
        groups.put("EAST", new ArrayList<>());
        groups.put("WEST", new ArrayList<>());

        for (Order order : orders) {
            double dLat = order.getToLatitude() - warehouseLat;
            double dLng = order.getToLongitude() - warehouseLng;

            if (Math.abs(dLat) > Math.abs(dLng)) {
                if (dLat >= 0) groups.get("NORTH").add(order);
                else groups.get("SOUTH").add(order);
            } else {
                if (dLng >= 0) groups.get("EAST").add(order);
                else groups.get("WEST").add(order);
            }
        }
        System.out.println("Direction groups: " +
            groups.entrySet().stream().map(e -> e.getKey() + "=" + e.getValue().size())
            .reduce((a, b) -> a + ", " + b).orElse(""));
        return groups;
    }

    public Map<Integer, List<Order>> splitByDistance(List<Order> orders, double warehouseLat, double warehouseLng) {
        Map<Integer, List<Order>> buckets = new HashMap<>();

        for (Order order : orders) {
            double dist = haversine(warehouseLat, warehouseLng, order.getToLatitude(), order.getToLongitude());
            int bucket = ((int) dist / 20 + 1) * 20; // group into 20km buckets
            buckets.computeIfAbsent(bucket, k -> new ArrayList<>()).add(order);
        }
        System.out.println("Distance buckets: " +
            buckets.entrySet().stream().map(e -> e.getKey() + "km=" + e.getValue().size())
            .reduce((a, b) -> a + ", " + b).orElse(""));
        return buckets;
    }

    public List<List<Order>> assignOrdersToTruck(List<Order> orders, double truckCapacity, int maxOrdersPerTruck) {
        List<List<Order>> trucks = new ArrayList<>();
        List<Order> currentTruck = new ArrayList<>();
        double currentWeight = 0;

        for (Order order : orders) {
            if (currentTruck.size() >= maxOrdersPerTruck || currentWeight + order.getPackageWeight() > truckCapacity) {
                trucks.add(currentTruck);
                currentTruck = new ArrayList<>();
                currentWeight = 0;
            }
            currentTruck.add(order);
            currentWeight += order.getPackageWeight();
        }
        if (!currentTruck.isEmpty()) trucks.add(currentTruck);

        System.out.println("Assigned to " + trucks.size() + " truck loads (capacity=" + truckCapacity + ")." + " orders weighted: " + currentWeight);
        return trucks;
    }

    public List<Order> optimizeRouteWithHeldKarp(List<Order> orders, double warehouseLat, double warehouseLng) {
        int n = orders.size();
        if (n == 0) return orders;

        // Precompute distance matrix (warehouse + orders)
        double[][] dist = new double[n + 1][n + 1]; // index 0 = warehouse
        for (int i = 0; i < n; i++) {
            dist[0][i + 1] = haversine(warehouseLat, warehouseLng,
                                    orders.get(i).getToLatitude(), orders.get(i).getToLongitude());
            dist[i + 1][0] = dist[0][i + 1];
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) continue;
                dist[i + 1][j + 1] = haversine(orders.get(i).getToLatitude(), orders.get(i).getToLongitude(),
                                            orders.get(j).getToLatitude(), orders.get(j).getToLongitude());
            }
        }

        // Held-Karp DP
        Map<Integer, Map<Integer, Double>> dp = new HashMap<>();
        Map<Integer, Map<Integer, Integer>> parent = new HashMap<>();

        for (int i = 1; i <= n; i++) {
            dp.computeIfAbsent(1 << (i - 1), k -> new HashMap<>()).put(i, dist[0][i]);
        }

        for (int mask = 1; mask < (1 << n); mask++) {
            for (int last = 1; last <= n; last++) {
                if ((mask & (1 << (last - 1))) == 0) continue;
                Double cost = dp.getOrDefault(mask, Collections.emptyMap()).get(last);
                if (cost == null) continue;

                for (int next = 1; next <= n; next++) {
                    if ((mask & (1 << (next - 1))) != 0) continue;
                    int nextMask = mask | (1 << (next - 1));
                    double newCost = cost + dist[last][next];
                    dp.computeIfAbsent(nextMask, k -> new HashMap<>());
                    double oldCost = dp.get(nextMask).getOrDefault(next, Double.POSITIVE_INFINITY);

                    if (newCost < oldCost) {
                        dp.get(nextMask).put(next, newCost);
                        parent.computeIfAbsent(nextMask, k -> new HashMap<>()).put(next, last);
                    }
                }
            }
        }

        int fullMask = (1 << n) - 1;
        double bestCost = Double.POSITIVE_INFINITY;
        int lastNode = -1;

        for (int i = 1; i <= n; i++) {
            Double cost = dp.getOrDefault(fullMask, Collections.emptyMap()).get(i);
            if (cost == null) continue;
            cost += dist[i][0];
            if (cost < bestCost) {
                bestCost = cost;
                lastNode = i;
            }
        }

        List<Integer> path = new ArrayList<>();
        int mask = fullMask;
        while (lastNode != -1) {
            path.add(lastNode);
            int prev = parent.getOrDefault(mask, Collections.emptyMap()).getOrDefault(lastNode, -1);
            mask = mask & ~(1 << (lastNode - 1));
            lastNode = prev;
        }
        Collections.reverse(path);

        List<Order> optimized = new ArrayList<>();
        for (int idx : path) {
            optimized.add(orders.get(idx - 1));
        }
        return optimized;
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // km
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon/2) * Math.sin(dLon/2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
    }

    public static class TruckRoute {
        private final Truck truck;
        private final List<Order> orders;
        private final String direction;
        private final int distanceBucket;

        public TruckRoute(Truck truck, List<Order> orders, String direction, int distanceBucket) {
            this.truck = truck;
            this.orders = orders;
            this.direction = direction;
            this.distanceBucket = distanceBucket;
        }

        public Truck getTruck() { return truck; }
        public List<Order> getOrders() { return orders; }
        public String getDirection() { return direction; }
        public int getDistanceBucket() { return distanceBucket; }
    }

    // ---------------- Final orchestrator ---------------- //

    public List<TruckRoute> routeWarehouseOrders(Long warehouseId, int maxOrdersPerTruck) {
        Warehouse warehouse = warehouseRepo.findById(warehouseId)
                .orElseThrow(() -> new RuntimeException("Warehouse not found"));

        double warehouseLat = warehouse.getLatitude();
        double warehouseLng = warehouse.getLongitude();

        List<Order> orders = getOrders(warehouseId);
        for(Order o : orders) {
            System.out.println("OrderID: " + o.getOrderId() + " | Addr: " + o.getDeliveryAddress() + 
                               " | Weight: " + o.getPackageWeight() + " | Value: " + o.getPackageValue() +
                               " | Dist: " + o.getDeliveryDistance() + " km"
            );
        }

        // Get available trucks for this warehouse
        List<Truck> availableTrucks = truckRepo.findByWarehouse_WarehouseIdAndStatus(
                warehouseId, Truck.Status.AVAILABLE);

        if (availableTrucks.isEmpty()) {
            System.out.println("No available trucks for warehouse " + warehouseId);
            return Collections.emptyList();
        }

        Iterator<Truck> truckIterator = availableTrucks.iterator();
        Map<String, List<Order>> byDirection = splitByDirection(orders, warehouseLat, warehouseLng);
        List<TruckRoute> result = new ArrayList<>();
        Set<Long> assignedOrders = new HashSet<>();

        for (Map.Entry<String, List<Order>> entry : byDirection.entrySet()) {
            String direction = entry.getKey();
            Map<Integer, List<Order>> byDistance = splitByDistance(entry.getValue(), warehouseLat, warehouseLng);

            for (Map.Entry<Integer, List<Order>> distanceEntry : byDistance.entrySet()) {
                int distanceBucket = distanceEntry.getKey();
                List<Order> bucketOrders = new ArrayList<>(distanceEntry.getValue());

                // Remove already assigned orders
                bucketOrders.removeIf(o -> assignedOrders.contains(o.getOrderId()));
                if (bucketOrders.isEmpty()) continue;

                while (!bucketOrders.isEmpty() && truckIterator.hasNext()) {
                    Truck truck = truckIterator.next();

                    List<List<Order>> truckLoads =
                            assignOrdersToTruck(bucketOrders, truck.getCapacityWeight(), maxOrdersPerTruck);

                    if (truckLoads.isEmpty()) continue;

                    List<Order> load = truckLoads.get(0);

                    bucketOrders.removeAll(load);
                    load.forEach(o -> assignedOrders.add(o.getOrderId()));

                    List<Order> optimized = optimizeRouteWithHeldKarp(load, warehouseLat, warehouseLng);
                    result.add(new TruckRoute(truck, optimized, direction, distanceBucket));
                }

                if (!truckIterator.hasNext() && !bucketOrders.isEmpty()) {
                    System.out.println("No more trucks available, stopping distribution.");
                    return result;
                }
            }
        }
        return result;
    }

}
