package fr.rom.gameoflife.property;

import fr.rom.gameoflife.utils.Message;
import fr.rom.gameoflife.utils.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class GameProps {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameProps.class);
    public static final GameProps INSTANCE = new GameProps();

    private final Properties gameProperties;

    private int initialMinCellWidth;
    private int initialMaxCellWidth;
    private int initialCellWidth;
    private int initialMinCellHeight;
    private int initialMaxCellHeight;
    private int initialCellHeight;
    private int initialMinNbColumns;
    private int initialMaxNbColumns;
    private int initialNbColumns;
    private int initialMinNbRows;
    private int initialMaxNbRows;
    private int initialNbRows;
    private String initialCellAliveColor;
    private String initialCellDeadColor;
    private String initialShapePath;
    private int initialRefreshTimeMs;
    private List<String> initialComesToLife;
    private List<String> initialStayAlive;

    private int minCellWidth = -1;
    private int maxCellWidth = -1;
    private int cellWidth = -1;
    private int minCellHeight = -1;
    private int maxCellHeight = -1;
    private int cellHeight = -1;
    private int minNbColumns = -1;
    private int maxNbColumns = -1;
    private int nbColumns = -1;
    private int minNbRows = -1;
    private int maxNbRows = -1;
    private int nbRows = -1;
    private String cellAliveColor;
    private String cellDeadColor;
    private String shapePath;
    private int refreshTimeMs = -1;
    private List<String> comesToLife;
    private List<String> stayAlive;

    private GameProps() {
        FileInputStream gameInputStream = null;
        try {
            String rootPath = Objects.requireNonNull(Thread.currentThread().getContextClassLoader().getResource("")).getPath();

            String gamePropertiesPath = rootPath + "game.properties";
            gameInputStream =  new FileInputStream(gamePropertiesPath);
            gameProperties = new java.util.Properties();
            gameProperties.load(gameInputStream);
            init();
        } catch (IOException | NullPointerException e) {
            LOGGER.error(Message.get("log.errorLoadingGameProperties"), e);
            throw new ExceptionInInitializerError(); //TODO throw autre chose.
        } finally {
            if (gameInputStream != null) {
                try {
                    gameInputStream.close();
                } catch (IOException e) {
                    LOGGER.error(e.getMessage(), e);
                }
            }
        }
    }

    public static GameProps get() {
        return INSTANCE;
    }

    private void init() {
        String key = null;
        try {
            key = "minCellWidth";
            initialMinCellWidth = Integer.parseInt(gameProperties.getProperty(key));
            key = "maxCellWidth";
            initialMaxCellWidth = Integer.parseInt(gameProperties.getProperty(key));
            key = "cellWidth";
            initialCellWidth = Integer.parseInt(gameProperties.getProperty(key));
            key = "minCellHeight";
            initialMinCellHeight = Integer.parseInt(gameProperties.getProperty(key));
            key = "maxCellHeight";
            initialMaxCellHeight = Integer.parseInt(gameProperties.getProperty(key));
            key = "cellHeight";
            initialCellHeight = Integer.parseInt(gameProperties.getProperty(key));
            key = "minNbColumns";
            initialMinNbColumns = Integer.parseInt(gameProperties.getProperty(key));
            key = "maxNbColumns";
            initialMaxNbColumns = Integer.parseInt(gameProperties.getProperty(key));
            key = "nbColumns";
            initialNbColumns = Integer.parseInt(gameProperties.getProperty(key));
            key = "minNbRows";
            initialMinNbRows = Integer.parseInt(gameProperties.getProperty(key));
            key = "maxNbRows";
            initialMaxNbRows = Integer.parseInt(gameProperties.getProperty(key));
            key = "nbRows";
            initialNbRows = Integer.parseInt(gameProperties.getProperty(key));
            key = "cellAliveColor";
            initialCellAliveColor = gameProperties.getProperty(key);
            key = "cellDeadColor";
            initialCellDeadColor = gameProperties.getProperty(key);
            key = "shapePath";
            initialShapePath = gameProperties.getProperty(key);
            key = "refreshTimeMs";
            initialRefreshTimeMs = Integer.parseInt(gameProperties.getProperty(key));
            key = "comesToLife";
            initialComesToLife = Arrays.asList(gameProperties.getProperty(key).split(Strings.COMMA));
            key = "stayAlive";
            initialStayAlive = Arrays.asList(gameProperties.getProperty(key).split(Strings.COMMA));
        } catch (NumberFormatException | NullPointerException e) {
            LOGGER.error(Message.get("log.badPropertyValue", key), e);
        }
    }

    public void save() {
        gameProperties.setProperty("minCellWidth", String.valueOf(getMinCellWidth()));
        gameProperties.setProperty("maxCellWidth", String.valueOf(getMaxCellWidth()));
        gameProperties.setProperty("cellWidth", String.valueOf(getCellWidth()));
        gameProperties.setProperty("minCellHeight", String.valueOf(getMinCellHeight()));
        gameProperties.setProperty("maxCellHeight", String.valueOf(getMaxCellHeight()));
        gameProperties.setProperty("cellHeight", String.valueOf(getCellHeight()));
        gameProperties.setProperty("minNbColumns", String.valueOf(getMinNbColumns()));
        gameProperties.setProperty("maxNbColumns", String.valueOf(getMaxNbColumns()));
        gameProperties.setProperty("nbColumns", String.valueOf(getNbColumns()));
        gameProperties.setProperty("minNbRows", String.valueOf(getMinNbRows()));
        gameProperties.setProperty("maxNbRows", String.valueOf(getMaxNbRows()));
        gameProperties.setProperty("nbRows", String.valueOf(getNbRows()));
        gameProperties.setProperty("cellAliveColor", getCellAliveColor());
        gameProperties.setProperty("cellDeadColor", getCellDeadColor());
        gameProperties.setProperty("shapePath", getShapePath());
        gameProperties.setProperty("refreshTimeMs", String.valueOf(getRefreshTimeMs()));
        gameProperties.setProperty("comesToLife", PropertyUtils.listToString(getComesToLife()));
        gameProperties.setProperty("stayAlive", PropertyUtils.listToString(getStayAlive()));
    }

    public int getMinCellWidth() {
        if(minCellWidth != -1) {
            return minCellWidth;
        }
        return initialMinCellWidth;
    }

    public int getMaxCellWidth() {
        if(maxCellWidth != -1) {
            return maxCellWidth;
        }
        return initialMaxCellWidth;
    }

    public int getCellWidth() {
        if(cellWidth != -1) {
            return cellWidth;
        }
        return initialCellWidth;
    }

    public int getMinCellHeight() {
        if(minCellHeight != -1) {
            return minCellHeight;
        }
        return initialMinCellHeight;
    }

    public int getMaxCellHeight() {
        if(maxCellHeight != -1) {
            return maxCellHeight;
        }
        return initialMaxCellHeight;
    }

    public int getCellHeight() {
        if(cellHeight != -1) {
            return cellHeight;
        }
        return initialCellHeight;
    }

    public int getMinNbColumns() {
        if(minNbColumns != -1) {
            return minNbColumns;
        }
        return initialMinNbColumns;
    }

    public int getMaxNbColumns() {
        if(maxNbColumns != -1) {
            return maxNbColumns;
        }
        return initialMaxNbColumns;
    }

    public int getNbColumns() {
        if(nbColumns != -1) {
            return nbColumns;
        }
        return initialNbColumns;
    }

    public int getMinNbRows() {
        if(minNbRows != -1) {
            return minNbRows;
        }
        return initialMinNbRows;
    }

    public int getMaxNbRows() {
        if(maxNbRows != -1) {
            return maxNbRows;
        }
        return initialMaxNbRows;
    }

    public int getNbRows() {
        if(nbRows != -1) {
            return nbRows;
        }
        return initialNbRows;
    }

    public String getCellAliveColor() {
        if(cellAliveColor != null) {
            return cellAliveColor;
        }
        return initialCellAliveColor;
    }

    public String getCellDeadColor() {
        if(cellDeadColor != null) {
            return cellDeadColor;
        }
        return initialCellDeadColor;
    }

    public String getShapePath() {
        if(shapePath != null) {
            return shapePath;
        }
        return initialShapePath;
    }

    public int getRefreshTimeMs() {
        if(refreshTimeMs != -1) {
            return refreshTimeMs;
        }
        return initialRefreshTimeMs;
    }

    public List<String> getComesToLife() {
        if(comesToLife != null) {
            return comesToLife;
        }
        return initialComesToLife;
    }

    public List<String> getStayAlive() {
        if(stayAlive != null) {
            return stayAlive;
        }
        return initialStayAlive;
    }

    public void setMinCellWidth(int minCellWidth) {
        this.minCellWidth = minCellWidth;
    }

    public void setMaxCellWidth(int maxCellWidth) {
        this.maxCellWidth = maxCellWidth;
    }

    public void setCellWidth(int cellWidth) {
        this.cellWidth = cellWidth;
    }

    public void setMinCellHeight(int minCellHeight) {
        this.minCellHeight = minCellHeight;
    }

    public void setMaxCellHeight(int maxCellHeight) {
        this.maxCellHeight = maxCellHeight;
    }

    public void setCellHeight(int cellHeight) {
        this.cellHeight = cellHeight;
    }

    public void setMinNbColumns(int minNbColumns) {
        this.minNbColumns = minNbColumns;
    }

    public void setMaxNbColumns(int maxNbColumns) {
        this.maxNbColumns = maxNbColumns;
    }

    public void setNbColumns(int nbColumns) {
        this.nbColumns = nbColumns;
    }

    public void setMinNbRows(int minNbRows) {
        this.minNbRows = minNbRows;
    }

    public void setMaxNbRows(int maxNbRows) {
        this.maxNbRows = maxNbRows;
    }

    public void setNbRows(int nbRows) {
        this.nbRows = nbRows;
    }

    public void setCellAliveColor(String cellAliveColor) {
        this.cellAliveColor = cellAliveColor;
    }

    public void setCellDeadColor(String cellDeadColor) {
        this.cellDeadColor = cellDeadColor;
    }

    public void setShapePath(String shapePath) {
        this.shapePath = shapePath;
    }

    public void setRefreshTimeMs(int refreshTimeMs) {
        this.refreshTimeMs = refreshTimeMs;
    }

    public void setComesToLife(List<String> comesToLife) {
        this.comesToLife = comesToLife;
    }

    public void addComesToLife(String comesToLife) {
        if(this.comesToLife == null) {
            this.comesToLife = new ArrayList<>(this.initialComesToLife);
        }
        if(this.comesToLife.contains(comesToLife)) return;
        this.comesToLife.add(comesToLife);
    }

    public void removeComesToLife(final String comesToLife) {
        if(this.comesToLife == null) {
            this.comesToLife = new ArrayList<>(this.initialComesToLife);
        }
        this.comesToLife.remove(comesToLife);
    }

    public void setStayAlive(List<String> stayAlive) {
        this.stayAlive = stayAlive;
    }

    public void addStayAlive(String stayAlive) {
        if(this.stayAlive == null) {
            this.stayAlive = new ArrayList<>(this.initialStayAlive);
        }
        if(this.stayAlive.contains(stayAlive)) return;
        this.stayAlive.add(stayAlive);
    }

    public void removeStayAlive(final String stayAlive) {
        if(this.stayAlive == null) {
            this.stayAlive = new ArrayList<>(this.initialStayAlive);
        }
        this.stayAlive.remove(stayAlive);
    }
}
