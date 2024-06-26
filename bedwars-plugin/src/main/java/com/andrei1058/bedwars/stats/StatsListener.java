/*
 * BedWars1058 - A bed wars mini-game.
 * Copyright (C) 2021 Andrei Dascălu
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Contact e-mail: andrew.dascalu@gmail.com
 */

package com.andrei1058.bedwars.stats;

import com.andrei1058.bedwars.BedWars;
import com.andrei1058.bedwars.api.arena.GameState;
import com.andrei1058.bedwars.api.arena.IArena;
import com.andrei1058.bedwars.api.arena.team.ITeam;
import com.andrei1058.bedwars.api.events.gameplay.GameEndEvent;
import com.andrei1058.bedwars.api.events.player.PlayerBedBreakEvent;
import com.andrei1058.bedwars.api.events.player.PlayerKillEvent;
import com.andrei1058.bedwars.api.events.player.PlayerLeaveArenaEvent;
import com.andrei1058.bedwars.api.stats.IPlayerStats;
import com.andrei1058.bedwars.arena.Arena;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class StatsListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPreLoginEvent(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            // Do nothing if login fails
            return;
        }
        IPlayerStats stats = BedWars.getRemoteDatabase().fetchStats(event.getUniqueId());
        stats.setName(event.getName());
        BedWars.getStatsManager().put(event.getUniqueId(), stats);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLoginEvent(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            // Prevent memory leak if login fails
            BedWars.getStatsManager().remove(event.getPlayer().getUniqueId());
        }
    }

    @EventHandler
    public void onBedBreak(PlayerBedBreakEvent event) {
        IPlayerStats stats = BedWars.getStatsManager().get(event.getPlayer().getUniqueId());
        //store beds destroyed
        stats.setBedsDestroyed(stats.getBedsDestroyed() + 1);
    }

    @EventHandler
    public void onBedLoss(PlayerBedBreakEvent event) {
        List<Player> victims = event.getVictimTeam().getMembers();
        for (Player victim : victims) {
            UUID victimUUID = victim.getUniqueId();
            IPlayerStats stats = BedWars.getStatsManager().get(victimUUID);
            BedWars.debug("PlayerBedBreakEvent event: Update player bed loss stats");
            //store bed losses
            stats.setBedsLost(stats.getBedsLost() + 1);
        }
    }

    @EventHandler
    public void onPlayerKill(PlayerKillEvent event) {
        IPlayerStats victimStats = BedWars.getStatsManager().get(event.getVictim().getUniqueId());
        // If killer is not null and not equal to victim
        IPlayerStats killerStats = !event.getVictim().equals(event.getKiller()) ?
                (event.getKiller() == null ? null : BedWars.getStatsManager().getUnsafe(event.getKiller().getUniqueId())) : null;

        if (event.getCause().isFinalKill()) {
            //store final deaths
            victimStats.setFinalDeaths(victimStats.getFinalDeaths() + 1);

            /* store losses
            *
            * victimStats.setLosses(victimStats.getLosses() + 1);
            *
            * This should not be here because, if a player is final killed,
            * he may have a teammate who can clutch up and win the game.
            * But even after that, he will be given the loss and that sucks.
            */

            //store games played
            victimStats.setGamesPlayed(victimStats.getGamesPlayed() + 1);
            //store final kills
            if (killerStats != null) killerStats.setFinalKills(killerStats.getFinalKills() + 1);
        } else {
            //store deaths
            victimStats.setDeaths(victimStats.getDeaths() + 1);
            //store kills
            if (killerStats != null) killerStats.setKills(killerStats.getKills() + 1);
        }
    }

    @EventHandler
    public void onGameEnd(GameEndEvent event) {
        String group = event.getArena().getGroup();
        for (UUID uuid : event.getLosers()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            if (!player.isOnline()) continue;

            IPlayerStats stats = BedWars.getStatsManager().getUnsafe(uuid);

            // store the loss even if is in another game
            if (stats != null) {
                stats.setLosses(stats.getLosses() + 1);
                stats.setWinStreak(stats.getWinStreak() - stats.getWinStreak());
                if (group.equals("Solo") || group.equals("Doubles") || group.equals("3v3v3v3") || group.equals("4v4v4v4") || group.equals("4v4")) {
                    stats.setOverAllWinStreak(0);
                }
                if (group.equals("Solo") || group.equals("Doubles") || group.equals("3v3v3v3") || group.equals("4v4v4v4")) {
                    stats.setCoreWinStreak(0);
                }
            }
        }

        for (UUID uuid : event.getWinners()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) continue;
            if (!player.isOnline()) continue;

            IPlayerStats stats = BedWars.getStatsManager().get(uuid);

            // store wins even if is in another game because he assisted this team
            // the ones who abandoned are already removed from the winners list
            stats.setWins(stats.getWins() + 1);
            stats.setWinStreak(stats.getWinStreak() + 1);

            if (group.equals("Solo") || group.equals("Doubles") || group.equals("3v3v3v3") || group.equals("4v4v4v4") || group.equals("4v4")) {
                stats.setOverAllWinStreak(stats.getOverAllWinStreak() + 1);
                if (stats.getOverAllHighestWinStreak() < stats.getOverAllWinStreak()) {
                    stats.setOverAllHighestWinStreak(stats.getOverAllWinStreak());
                }
            }
            if (group.equals("Solo") || group.equals("Doubles") || group.equals("3v3v3v3") || group.equals("4v4v4v4")) {
                stats.setCoreWinStreak(stats.getCoreWinStreak() + 1);
                if (stats.getCoreHighestWinStreak() < stats.getCoreWinStreak()) {
                    stats.setCoreHighestWinStreak(stats.getCoreWinStreak());
                }
            }
            if (stats.getHighestWinStreak() < stats.getWinStreak()) {
                stats.setHighestWinStreak(stats.getWinStreak());
            }

            // store games played
            // give if he remained in this arena till the end even if was eliminated
            // for those who left games played are updated in arena leave listener
            IArena playerArena = Arena.getArenaByPlayer(player);
            if (playerArena != null && playerArena.equals(event.getArena())) {
                stats.setGamesPlayed(stats.getGamesPlayed() + 1);
            }
        }
    }

    @EventHandler
    public void onArenaLeave(@NotNull PlayerLeaveArenaEvent event) {
        final Player player = event.getPlayer();

        ITeam team = event.getArena().getExTeam(player.getUniqueId());
        if (team == null) {
            return; // The player didn't play this game
        }

        if (event.getArena().getStatus() == GameState.starting || event.getArena().getStatus() == GameState.waiting) {
            return; // Game didn't start
        }

        IPlayerStats playerStats = BedWars.getStatsManager().getUnsafe(player.getUniqueId());
        // sometimes can be null due to scheduling delays
        if (playerStats == null) {
            return;
        }

        // Update last play and first play (if required)
        Instant now = Instant.now();
        playerStats.setLastPlay(now);
        if (playerStats.getFirstPlay() == null) {
            playerStats.setFirstPlay(now);
        }

        // 28/10/2023 this should be handled by PlayerKillEvent now. It wasn't called for pvp log out before.
        // Check quit abuse
//        if (event.getArena().getStatus() == GameState.playing) {
//            // Only if the player left the arena while the game was running
//            if (team.isBedDestroyed()) {
//                // Only if the team had the bed destroyed
//
//                // Punish player if bed is destroyed and he disconnects without getting killed
//                // if he is not in the spectators list it means he did not pass trough player kill event and he did not receive
//                // the penalty bellow.
//                if (event.getArena().isPlayer(player)) {
//                    playerStats.setFinalDeaths(playerStats.getFinalDeaths() + 1);
//                    playerStats.setLosses(playerStats.getLosses() + 1);
//                }
//
//                // Reward attacker
//                // if attacker is not null it means the victim did pvp log out
//                Player damager = event.getLastDamager();
//                ITeam killerTeam = event.getArena().getTeam(damager);
//                if (damager != null && event.getArena().isPlayer(damager) && killerTeam != null) {
//                    PlayerStats damagerStats = BedWars.getStatsManager().get(damager.getUniqueId());
//                    damagerStats.setFinalKills(damagerStats.getFinalKills() + 1);
//                    event.getArena().addPlayerKill(damager, true, player);
//                }
//            } else {
//                // Prevent pvp log out abuse
//                Player damager = event.getLastDamager();
//                ITeam killerTeam = event.getArena().getTeam(damager);
//
//                // killer is null if if he already received kill point.
//                // LastHit damager is set to null at PlayerDeathEvent so this part is not duplicated for sure.
//                // damager is not null if the victim disconnected during pvp only.
//                if (event.getLastDamager() != null && event.getArena().isPlayer(damager) && killerTeam != null) {
//                    // Punish player
//                    playerStats.setDeaths(playerStats.getDeaths() + 1);
//                    event.getArena().addPlayerDeath(player);
//
//                    // Reward attacker
//                    event.getArena().addPlayerKill(damager, false, player);
//                    PlayerStats damagerStats = BedWars.getStatsManager().get(damager.getUniqueId());
//                    damagerStats.setKills(damagerStats.getKills() + 1);
//                }
//            }
//        }

        //save or replace stats for player - run later because PlayerKillEvent is triggered after PlayerLeaveArenaEvent
        Bukkit.getScheduler().runTaskLaterAsynchronously(BedWars.plugin, () -> BedWars.getRemoteDatabase().saveStats(playerStats), 10L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(@NotNull PlayerQuitEvent event) {
        BedWars.getStatsManager().remove(event.getPlayer().getUniqueId());
    }
}
