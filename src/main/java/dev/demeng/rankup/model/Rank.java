package dev.demeng.rankup.model;

import dev.demeng.demlib.item.ItemCreator;
import dev.demeng.demlib.message.MessageUtils;
import dev.demeng.rankup.Rankup;
import dev.demeng.rankup.preferences.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class Rank {

  @Getter private final String name;

  @Getter private final int slot;

  @Getter private final double cost;

  @Getter private final List<String> commands;

  @Getter private final ItemStack material;

  @Getter private final String displayName;

  public static Rank of(RankedPlayer p, String rankName) {

    final FileConfiguration settings = Rankup.getInstance().getSettings();
    final String path = "ranks." + rankName + ".";

    final int cSlot = settings.getInt(path + "slot");
    final double cCost = calculateCost(rankName, p.getPrestigeNumber());
    final List<String> cCommands = new ArrayList<>();

    for (String cmd : settings.getStringList(path + "commands")) {
      cCommands.add(cmd.replace("%player%", p.getName()));
    }

    final ItemStack cMaterial;
    final String cDisplayName;

    if (p.getRankName().equals(rankName)) {
      cMaterial = ItemCreator.getMaterial(settings.getString("rank-materials.current"));
      cDisplayName =
          setPlaceholders(
              p.getPlayer(), settings.getString("rank-display-names.current"), rankName, cCost);

    } else if (getNextRankName(p.getRankName()).equals(rankName)) {
      cMaterial = ItemCreator.getMaterial(settings.getString("rank-materials.next"));
      cDisplayName =
          setPlaceholders(
              p.getPlayer(), settings.getString("rank-display-names.next"), rankName, cCost);

    } else if (getPreviousRankNames(p.getRankName()).contains(rankName)) {
      cMaterial = ItemCreator.getMaterial(settings.getString("rank-materials.completed"));
      cDisplayName =
          setPlaceholders(
              p.getPlayer(), settings.getString("rank-display-names.completed"), rankName, cCost);

    } else {
      cMaterial = ItemCreator.getMaterial(settings.getString("rank-materials.not-found"));
      cDisplayName =
          setPlaceholders(
              p.getPlayer(), settings.getString("rank-display-names.not-found"), rankName, cCost);
    }

    return new Rank(rankName, cSlot, cCost, cCommands, cMaterial, cDisplayName);
  }

  public void give(Player p) {

    final Economy eco = Rankup.getInstance().getEconomy();

    p.closeInventory();

    if (!eco.has(p, cost)) {
      MessageUtils.tell(p, setPlaceholders(p, Message.INSUFFICIENT_BALANCE, name, cost));
      return;
    }

    new RankedPlayer(p).setRank(name);

    Rankup.getInstance().getEconomy().withdrawPlayer(p, cost);

    for (String cmd : commands) {
      Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
    }

    MessageUtils.broadcast(
        Message.RANKED_UP.replace("%player%", p.getName()).replace("%rank%", name));
  }

  private static String setPlaceholders(Player p, String s, String name, double cost) {
    final DecimalFormat formatter = new DecimalFormat("#,###.##");
    return s.replace("%rank%", name)
        .replace("%cost%", formatter.format(cost))
        .replace("%balance%", formatter.format(Rankup.getInstance().getEconomy().getBalance(p)));
  }

  public static String getNextRankName(String rankName) {
    if (rankName.equals("Free")) return "Nothing";
    return getRankNames().get(getRankNames().indexOf(rankName) + 1);
  }

  public static List<String> getPreviousRankNames(String rankName) {
    final List<String> names = new ArrayList<>();
    for (int i = getRankNames().indexOf(rankName) - 1; i >= 0; i--)
      names.add(getRankNames().get(i));
    return names;
  }

  public static double calculateCost(String rankName, int prestige) {
    final double og = Rankup.getInstance().getSettings().getInt("ranks." + rankName + ".cost");
    if (prestige == 0) return og;
    return og * prestige + og;
  }

  public static List<String> getRankNames() {
    final List<String> names = new ArrayList<>();

    names.add("A");
    names.add("B");
    names.add("C");
    names.add("D");
    names.add("E");
    names.add("F");
    names.add("G");
    names.add("H");
    names.add("I");
    names.add("J");
    names.add("K");
    names.add("L");
    names.add("M");
    names.add("N");
    names.add("O");
    names.add("P");
    names.add("Q");
    names.add("R");
    names.add("S");
    names.add("T");
    names.add("U");
    names.add("V");
    names.add("W");
    names.add("X");
    names.add("Y");
    names.add("Z");
    names.add("Free");

    return names;
  }
}