import com.mysql.cj.jdbc.Driver;

import java.sql.*;
import java.util.HashMap;
import java.util.Objects;
import java.util.Scanner;

public class Main {
    private final static String URL = "jdbc:mysql://localhost:3306/test";
    private final static String USERNAME = "root";
    private final static String PASSWORD = "root";


    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            Driver driver = new Driver();
            DriverManager.registerDriver(driver);
        } catch (SQLException e) {
            System.out.println("!! Some troubles with driver manager");
        }

        String choose = "";

        try (Connection connection = DriverManager.getConnection(URL, USERNAME, PASSWORD); Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS notes_table(id INT PRIMARY KEY AUTO_INCREMENT, name_author VARCHAR(50), text_note VARCHAR(100))");
            while (!Objects.equals(choose, "5")) {

                System.out.println("---------------------------------");
                System.out.println("Введите операцию:           ");
                System.out.println("1. Создать новую заметку");
                System.out.println("2. Редактировать текущую заметку");
                System.out.println("3. Вывести все свои заметки");
                System.out.println("4. Удалить заметку");
                System.out.println("5. Конец");
                System.out.println("---------------------------------");

                choose = sc.nextLine();
                try {
                    int intChoose = Integer.parseInt(choose);
                    switch (intChoose) {
                        case (1) -> createNewNote(sc, connection);
                        case (2) -> updateNote(sc, connection, statement);
                        case (3) -> selectAllNotes(sc, connection, statement);
                        case (4) -> deleteNote(sc, connection, statement);
                        case (5) -> {
                            System.out.println("До свидания");
                            System.exit(0);
                        }
                        default -> System.out.println("!! Введите число от 1 до 5");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("!! Введите число");
                }
            }
        } catch (SQLException e) {
            System.out.println("!! Some troubles with connection");
        }
    }

    private static void createNewNote(Scanner sc, Connection connection) {
        try {
            PreparedStatement preparedStatement;
            System.out.println("Опция 'создать новую заметку'");
            System.out.println("Введите имя, для которого хотите создать заметку: ");
            String name_author = sc.nextLine();
            System.out.println("Введите текст заметки");
            String text_note = sc.nextLine();
            preparedStatement = connection.prepareStatement("INSERT INTO notes_table(name_author, text_note) VALUES(?, ?)");
            preparedStatement.setString(1, name_author);
            preparedStatement.setString(2, text_note);
            preparedStatement.execute();
        } catch (SQLException e) {
            System.out.println("!! Some troubles with prepared statement");
        }
    }


    private static void updateNote(Scanner sc, Connection connection, Statement statement) {
        try {
            PreparedStatement preparedStatement;
            System.out.println("Опция 'редактировать заметку'");
            System.out.println("Введите имя, для которого хотите редактировать заметку");
            String name_author = sc.nextLine();
            preparedStatement = connection.prepareStatement("SELECT * FROM notes_table WHERE name_author = ?;");
            preparedStatement.setString(1, name_author);
            ResultSet resultSet = preparedStatement.executeQuery();
            int ind = 1;
            if (!resultSet.next()) {
                System.out.println("Извините, на данное имя заметок не создано");
            } else {
                System.out.println("Выберите вашу заметку, которую вы хотите изменить: ");
                HashMap<Integer, Integer> notesToChange = new HashMap<>();
                int id = resultSet.getInt(1);
                notesToChange.put(ind, id);
                String name_author_table = resultSet.getString(2);
                String text_note_table = resultSet.getString(3);
                System.out.println(ind + ": " + text_note_table);
                ind++;
                while (resultSet.next()) {
                    id = resultSet.getInt(1);
                    notesToChange.put(ind, id);
                    name_author_table = resultSet.getString(2);
                    text_note_table = resultSet.getString(3);
                    System.out.println(ind + ": " + text_note_table);
                    ind++;
                }

                boolean fl = false;
                while (!fl) {
                    String noteToChange = sc.nextLine();
                    try {
                        int intNoteToChange = Integer.parseInt(noteToChange);
                        if (intNoteToChange > 0 && intNoteToChange < ind) {
                            fl = true;
                            int idNoteToChange = notesToChange.get(intNoteToChange);
                            System.out.println("Введите текст для новой заметки:");
                            String new_text_note = sc.nextLine();
                            preparedStatement = connection.prepareStatement("UPDATE notes_table SET text_note = ? WHERE id = ?");
                            preparedStatement.setString(1, new_text_note);
                            preparedStatement.setInt(2, idNoteToChange);
                            preparedStatement.execute();
                        } else {
                            System.out.println("!! Введите число от 1 до " + (ind - 1));
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("!! Введите число");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("!! Some troubles with prepared statement");
        }
    }


    private static void selectAllNotes(Scanner sc, Connection connection, Statement statement) {
        try {
            PreparedStatement preparedStatement;
            System.out.println("Введите имя, для которого хотите вывести все заметки:");
            String name_author = sc.nextLine();
            preparedStatement = connection.prepareStatement("SELECT * FROM notes_table WHERE name_author = ?;");
            preparedStatement.setString(1, name_author);
            ResultSet resultSet = preparedStatement.executeQuery();
            int ind = 1;
            while (resultSet.next()) {
                int id = resultSet.getInt(1);
                String name_author_table = resultSet.getString(2);
                String text_note_table = resultSet.getString(3);
                System.out.println(ind + ": " + text_note_table);
                ind++;
            }
            if (ind == 1) {
                System.out.println("Извините, заметок на имя " + name_author + " не найдено");
            }
        } catch (SQLException e) {
            System.out.println("!! Some troubles with prepared statement");
        }
    }


    private static void deleteNote(Scanner sc, Connection connection, Statement statement) {
        try {
            PreparedStatement preparedStatement;
            System.out.println("Введите имя, для которого хотите удалить заметку:");
            String name_author = sc.nextLine();
            preparedStatement = connection.prepareStatement("SELECT * FROM notes_table WHERE name_author = ?;");
            preparedStatement.setString(1, name_author);
            ResultSet resultSet = preparedStatement.executeQuery();
            int ind = 1;
            if (!resultSet.next()) {
                System.out.println("Извините, на данное имя заметок не создано");
            } else {
                System.out.println("Выберите заметку, которую хотите удалить:");
                HashMap<Integer, Integer> notesToDelete = new HashMap<>();
                int id = resultSet.getInt(1);
                notesToDelete.put(ind, id);
                String name_author_table = resultSet.getString(2);
                String text_note_table = resultSet.getString(3);
                System.out.println(ind + ": " + text_note_table);
                ind++;
                while (resultSet.next()) {
                    id = resultSet.getInt(1);
                    notesToDelete.put(ind, id);
                    name_author_table = resultSet.getString(2);
                    text_note_table = resultSet.getString(3);
                    System.out.println(ind + ": " + text_note_table);
                    ind++;
                }

                boolean fl = false;
                while (!fl) {
                    String noteToChange = sc.nextLine();
                    try {
                        int intNoteToChange = Integer.parseInt(noteToChange);
                        if (intNoteToChange > 0 && intNoteToChange < ind) {
                            fl = true;
                            int idNoteToChange = notesToDelete.get(intNoteToChange);
                            preparedStatement = connection.prepareStatement("DELETE FROM notes_table WHERE id = ?");
                            preparedStatement.setInt(1, idNoteToChange);
                            preparedStatement.execute();
                        } else {
                            System.out.println("!! Введите число от 1 до " + (ind - 1));
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("!! Введите число");
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("!! Some troubles with prepared statement");
        }
    }
}
