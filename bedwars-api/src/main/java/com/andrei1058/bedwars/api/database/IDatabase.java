package com.andrei1058.bedwars.api.database;

import com.andrei1058.bedwars.api.shop.IQuickBuyElement;
import com.andrei1058.bedwars.api.stats.IPlayerStats;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

/**
 * Represents a database interface for managing player statistics and data.
 */
public interface IDatabase {

    /**
     * Initializes the database.
     */
    void init();

    /**
     * Checks if a player has remote statistics.
     *
     * @param uuid The UUID of the player.
     * @return true if the player has remote statistics, false otherwise.
     */
    boolean hasStats(UUID uuid);

    /**
     * Creates or replaces statistics for a player.
     *
     * @param stats The player statistics to save.
     */
    void saveStats(IPlayerStats stats);

    /**
     * Fetches the player statistics for the given UUID.
     *
     * @param uuid The UUID of the player.
     * @return The player statistics for the specified UUID.
     */
    IPlayerStats fetchStats(UUID uuid);

    /**
     * Saves a custom statistic for a player.
     *
     * @param columnName The name of the custom column to save the statistic in.
     * @param player     The UUID of the player.
     * @param value      The value of the custom statistic.
     * @param dataType   The data type of the custom statistic.
     */
    void saveCustomStat(String columnName, UUID player, Object value, String dataType);

    /**
     * Retrieves a custom statistic for a player.
     *
     * @param columnName The name of the custom column to retrieve the statistic from.
     * @param player     The UUID of the player.
     * @return The value of the custom statistic, or null if the statistic is not found.
     */
    Object getCustomStat(String columnName, UUID player);

    /**
     * Gets the value of the quick buy slot for a player.
     *
     * @param uuid The UUID of the player.
     * @param slot The identifier of the quick buy slot.
     * @return The value of the quick buy slot.
     */
    String getQuickBuySlots(UUID uuid, int slot);

    /**
     * Gets the values of multiple quick buy slots for a player.
     *
     * @param uuid  The UUID of the player.
     * @param slots An array of identifiers of the quick buy slots.
     * @return A HashMap containing the slot identifier as the key and the slot value as the value.
     */
    HashMap<Integer, String> getQuickBuySlots(UUID uuid, int[] slots);

    /**
     * Checks if a player has any quick buy slots.
     *
     * @param player The UUID of the player.
     * @return true if the player has quick buy slots, false otherwise.
     */
    boolean hasQuickBuy(UUID player);

    /**
     * Gets the value of a statistics column for the given player.
     *
     * @param player The UUID of the player.
     * @param column The name of the statistics column.
     * @return The value of the statistics column.
     */
    @SuppressWarnings("unused")
    int getColumn(UUID player, String column);

    /**
     * Gets the player's level and experience points (XP).
     *
     * @param player The UUID of the player.
     * @return An Object array containing the player's level, XP, display name, and next level cost.
     *         Index 0: player level (int)
     *         Index 1: player XP (int)
     *         Index 2: display name (String)
     *         Index 3: next level cost (int)
     */
    Object[] getLevelData(UUID player);

    /**
     * Sets the level data for a player.
     *
     * @param player      The UUID of the player.
     * @param level       The player's level.
     * @param xp          The player's experience points (XP).
     * @param displayName The display name associated with the player's level.
     * @param nextCost    The cost required for the next level.
     */
    void setLevelData(UUID player, int level, int xp, String displayName, int nextCost);

    /**
     * Sets the language for a player.
     *
     * @param player The UUID of the player.
     * @param iso    The ISO code representing the player's language.
     */
    void setLanguage(UUID player, String iso);

    /**
     * Gets the language of a player.
     *
     * @param player The UUID of the player.
     * @return The ISO code representing the player's language.
     */
    String getLanguage(UUID player);

    /**
     * Pushes the changes to the quick buy slots for a player.
     *
     * @param updateSlots  A HashMap where the key is the slot ID and the value is the element.
     * @param uuid         The UUID of the player.
     * @param elementList  A list of quick buy elements.
     */
    void pushQuickBuyChanges(HashMap<Integer, String> updateSlots, UUID uuid, List<IQuickBuyElement> elementList);
}
