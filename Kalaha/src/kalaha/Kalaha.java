/**
 * This is a Java implementation of the game Kalaha (also known as Kalah, Kalahari, Warri, Owari).
 * The game includes a few different settings for longetivity, like random distributionű
 * of balls (or seeds) in the bowls, 
 */
package kalaha;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 *
 * @author Matt
 */
public class Kalaha {
    
    public static int turncount = 0;
    //This keeps track of who's turn is it currently - either 0 or 1.
    public static int yourturn = 1;
    //This sets who gets the first turn.
    public static int firstTurn = 1;
    //Shows when the game has ended.
    public static boolean isEnded = false;
    //This boolean shows whether a side has zero balls or not.
    public static boolean isAllZero = true;

    //In the UI, this is the 'top' player.
    public static boolean isplayer1AI = true;
    public static int player1AILevel = 1;

    //In the UI, this is the 'bottom' player.
    public static boolean isplayer2AI = false;
    public static int player2AILevel = 1;
    
    //Shows whether the balls are randomly distributed within the bowls.
    //If false, it will be equally distributed.
    public static boolean isRandom = false;
    //Shows how random is the distribution.
    public static int randomLevel = 0;
    //How many balls will be placed in a bowl, on average.
    public static int marbleAverage = 6;

    // 0-5 is enemy bowls, 7-12 is yours; 6 is enemy bigbowl, 13 is yours.
    public static int[] bowlNumbers = new int[14];
    
    //This sets whether a UI is shown - can be changed only at variable declaration (aka here).
    public static boolean isUI_Enabled = true;
    //Variables to be implemented with an AI.
    public static long repeatCount = 10000;
    public static long fitness1 = 0;
    public static long fitness2 = 0;
    
    /**
     * Upon startup, the main method checks whether this session is with a UI or not.
     * In the latter case, the games are automated and repeated.
     * @param args
     * @throws IOException 
     */
    public static void main(String[] args) throws IOException {
        
        //This condition is there just to avoid editing too much code.
        if ((isplayer1AI & isplayer2AI) == true) {
            isUI_Enabled = false;
        }
        
        if (isUI_Enabled) {
            //Open game window.
            withUI(args);
        }
        else {
            //Run an automated session - for a machine learning run.
            double winP1 = 0;
            double winP2 = 0;
            double win = 0;
            long tenPercent = repeatCount / 10;
            int hit = 0;
            for (long i = 0; i < repeatCount; i++) {
                win = withoutUI();
                winP1 += 1 - win;
                winP2 += win;
                
                //For longer sessions, it can be taken out, but doesn't hurt.
                if (i % tenPercent == 0 && i != 0) {
                    hit++;
                    //System.out.println((hit*10) + "% done..." );
                }
            }
            //Some results from the fitting.
            System.out.println("Result: " + winP1 + " vs " + winP2);
            System.out.println("Fitness for 1st AI:\t" + fitness1);
            System.out.println("Fitness for 2nd AI:\t" + fitness2);
        }
    }
    
    /**
     * This method is set as the basic to show how the program works.
     * @param args
     * @throws IOException 
     */
    public static void withUI(String[] args) throws IOException {
        reset();
        kgui.main(args);
    }

    /**
     * This method runs only through editing this projects main() method;
     *  to be used as testing the AIs.
     * @return points
     * @throws java.io.IOException
     */
    public static double withoutUI() throws IOException {
        reset();
        firstTurn = 1 - firstTurn;
        while (!isEnded(bowlNumbers)) {
            doTurn(doRobotTurn());
        }
        
        fitness1 += bowlNumbers[6] * bowlNumbers[6] * bowlNumbers[6];
        fitness2 += bowlNumbers[13] * bowlNumbers[13] * bowlNumbers[13];
        
        if (bowlNumbers[6] > bowlNumbers[13]) return 0;
        else if (bowlNumbers[6] < bowlNumbers[13]) return 1;
        else return 0.5;
    }

    /**This method takes the chosen bowl, counts down the balls according to the game rules,
     * and then checks whether the game should end.
     * 
     * If the countdown ends in the current player's bowl, it will not pass the turn to the other player.
     * 
     * @param callerID bowl ID
     * @throws IOException 
     */
    public static void doTurn(int callerID) throws IOException {
        int counter = bowlNumbers[callerID];
        int currentBowl = callerID + 1;
        boolean repeatTurn = false;

        bowlNumbers[callerID] = 0;
        while (counter > 0) {
            counter--;
            
            //Checking lower side bowls.
            if (currentBowl < 6) {
                bowlNumbers[currentBowl]++;
                checkIfLastBallInEmptyBowl(counter, currentBowl);
            //Checking upper side.
            } else if (currentBowl > 6 && currentBowl < 13) {
                bowlNumbers[currentBowl]++;
                checkIfLastBallInEmptyBowl(counter, currentBowl);
            //If this was the last one, and it's the player's big bowl, repeat turn.
            } else if ((currentBowl == 6 && yourturn == 0) || (currentBowl == 13 && yourturn == 1)) {
                bowlNumbers[currentBowl]++;
                if (counter == 0) {
                    repeatTurn = true;
                }
            } else {
                //If it just happens to be the big bowl, but not the last ball, proceed as normal.
                currentBowl++;
                currentBowl %= 14;
                bowlNumbers[currentBowl]++;
                checkIfLastBallInEmptyBowl(counter, currentBowl);
            }

            currentBowl++;
            currentBowl %= 14;
        }

        if (!repeatTurn) {
            yourturn = 1 - yourturn;
        }

        if (isUI_Enabled) {
            kgui.updateUI();
            kgui.turnStarter(yourturn);
        }

    }

    /**
     * This method checks whether the last ball was placed in the player's side in an empty bowl,
     * and does all related operations - moving the last ball and the balls on the opposite side to that player's bowl.
     *
     * @param counter
     * @param currentBowl
     */
    public static void checkIfLastBallInEmptyBowl(int counter, int currentBowl) {
        if (counter == 0 && bowlNumbers[currentBowl] == 1 && (currentBowl > 7 * yourturn - 1 && currentBowl < 7 * yourturn + 6)) {
            bowlNumbers[currentBowl] = 0;
            bowlNumbers[6 + 7 * yourturn] += bowlNumbers[12 - currentBowl] + 1;
            //Opponent bowl.
            bowlNumbers[12 - currentBowl] = 0;
        }
    }

    /**
     * As the name suggests, this method resets the game.
     */
    public static void reset() {
        yourturn = firstTurn;

        if (!isRandom) {
            Arrays.fill(bowlNumbers, 0, bowlNumbers.length - 1, marbleAverage);
            bowlNumbers[6] = 0;
            bowlNumbers[13] = 0;
        } else {
            //While the balls are randomly distributed within the bowls, both
            //  players have a symmetric setup.
            Random r = new Random();
            int toDistribute;
            int disp;
            //Highly random distribution; number of balls in a bowl can be between
            //  0 and 6 times the average.
            if (randomLevel == 1) {
                toDistribute = 6 * marbleAverage;
                Arrays.fill(bowlNumbers, 0, bowlNumbers.length - 1, 0);

                for (int i = 0; i < toDistribute; i++) {
                    disp = r.nextInt(6);
                    bowlNumbers[disp]++;
                    bowlNumbers[disp + 7]++;
                }
            } else {
                //This is a 'lightly' random distribution; the minimum is half of the average.
                toDistribute = marbleAverage / 2;
                toDistribute *= 6;
                Arrays.fill(bowlNumbers, 0, bowlNumbers.length - 1, marbleAverage - (marbleAverage / 2));

                for (int i = 0; i < toDistribute; i++) {
                    disp = r.nextInt(6);
                    bowlNumbers[disp]++;
                    bowlNumbers[disp + 7]++;
                }
            }
            
            //The big bowls should have 0 balls at the start.
            bowlNumbers[6] = 0;
            bowlNumbers[13] = 0;
        }

    }

    /**
     * Returns a boolean that shows whether the game ended.
     * One side has to have 0 balls in every 'home' bowl.
     *
     * @param bowlNumbers
     * @return Boolean. See method description above.
     */
    public static boolean isEnded(int[] bowlNumbers) {
        for (int i = 0; i < 2; i++) {
            isAllZero = true;
            for (int j = 0; j < 6; j++) {
                if (bowlNumbers[i * 7 + j] > 0) {
                    isAllZero = false;
                    break;
                }
            }
            if (isAllZero) {
                resolveEnding(bowlNumbers, i);
                return true;
            }
        }

        return false;
    }
    /**
     * This method is called when a side has no 'balls' left - all the remaining
     * are placed in the opposing player's 'big' bowl.
     * @param bowlNumbers
     * @param ending
     */
    public static void resolveEnding(int[] bowlNumbers, int ending) {
        int sum = 0;
        for (int i = 0; i < 6; i++) {
            sum += bowlNumbers[((1 - ending) * 7) + 5 - i];
            bowlNumbers[((1 - ending) * 7) + 5 - i] = 0;
        }
        bowlNumbers[6 + ending * 7] += sum;
    }

    /**
     * This method tells the game engine about the specific AI's decision.
     * As a note: working on the brainAI(); this will be a machine learning-based AI.
     *
     * @return integer signifying which bowl is chosen by the AI
     */
    public static int doRobotTurn() {

        int dec = -1;

        if (yourturn == 0) {
            if (player1AILevel == 0) {
                dec = randomAI();
            } else if (player1AILevel == 1) {
                dec = baseAI();
            } else {
                dec = brainAI();
            }
        } else {
            if (player2AILevel == 0) {
                dec = randomAI();
            } else if (player2AILevel == 1) {
                dec = baseAI();
            } else {
                dec = brainAI();
            }
        }

        return dec;
    }

    /**
     * This AI makes random decisions.
     *
     * @return int showing the index of chosen bowl
     */
    public static int randomAI() {
        Random r = new Random();

        int dec = r.nextInt(5);
        int beginFrom = r.nextInt(6);

        dec++;

        while (dec > 0) {
            if (bowlNumbers[7 * yourturn + ((beginFrom + 1)%6)] > 0) {
                dec--;
            }
            beginFrom++;
            beginFrom %= 6;
        }

        return beginFrom + 7 * yourturn;
    }

    /**
     * This AI tries to get another turn (if there is an obvious choice), but else it's random.
     *
     */
    public static int baseAI() {
        int dec = -1;
        
        for (int i = 0; i < 6; i++) {
            if (((bowlNumbers[7*yourturn + i] % 13) % 7) == 6 - i ) {
                dec = 7 * yourturn + i;
                //There is no 'break' clause, as it is better to pick a bowl closer to the player's bowl.
                //  (That lets the player to play more turns - there is no upper
                //  limit, as long as the last ball is placed in the big bowl.)
            }
        }
        
        if (dec == -1) {
            dec = randomAI();
        }
        
        return dec;
    }

    /**
     * This method will be a machine learning-based AI.
     * @return 
     */
    public static int brainAI() {

        return -1;
    }
}
