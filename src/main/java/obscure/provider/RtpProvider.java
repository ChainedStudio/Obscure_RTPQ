package obscure.provider;

import org.bukkit.World;
import org.bukkit.Location;
import java.util.concurrent.CompletableFuture;

public interface RtpProvider {
    // Generates a random safe location asynchronously
    CompletableFuture<Location> getRandomLocation(World world);
}