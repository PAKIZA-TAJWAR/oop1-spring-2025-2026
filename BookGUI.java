
package librarymanagementsystem.gui;
import librarymanagementsystem.entity.Book;
import librarymanagementsystem.fileio.BookFileIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;

public class BookGUI extends JFrame {

    private JTextField idField;         
    private JTextField titleField;     
    private JTextField authorNameField; 
    private JTextField categoryField;   
    private JTextField searchField;     

    private JTable table;                  
    private DefaultTableModel tableModel;  

    public BookGUI() 
    {
        setTitle("Library Management System");

        setSize(1000, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        JPanel inputPanel = new JPanel(new GridLayout(3, 1, 12, 10));
        inputPanel.setBorder(BorderFactory.createTitledBorder("Book Details"));

        inputPanel.add(new JLabel("Book ID (exactly 8 digits):"));
        idField = new JTextField();
        inputPanel.add(idField);
        inputPanel.add(new JLabel("Title:"));
        titleField = new JTextField();
        inputPanel.add(titleField);

        inputPanel.add(new JLabel("Author Name:"));
        authorNameField = new JTextField();
        inputPanel.add(authorNameField);

        inputPanel.add(new JLabel("Category:"));
        categoryField = new JTextField();
        inputPanel.add(categoryField);

        JPanel searchPanel = new JPanel(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search (by Title or Author)"));

        searchField = new JTextField();
        JButton searchBtn = new JButton("Search Book");

        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchBtn, BorderLayout.WEST);

       
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

        JButton addBtn      = new JButton("Add Book");       
        JButton updateBtn   = new JButton("Update Book");    
        JButton deleteBtn   = new JButton("Delete Book");    
        JButton viewAllBtn  = new JButton("View All Book");  
        JButton clearBtn    = new JButton("Clear");          

        buttonPanel.add(addBtn);
        buttonPanel.add(updateBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(viewAllBtn);
        buttonPanel.add(clearBtn);

       
        JPanel belowPanel = new JPanel(new BorderLayout(5, 5));
        belowPanel.add(inputPanel, BorderLayout.CENTER);
        belowPanel.add(searchPanel, BorderLayout.SOUTH);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(belowPanel, BorderLayout.CENTER);
        southPanel.add(buttonPanel, BorderLayout.SOUTH);

        String[] columns = { "ID", "Title", "Author Name", "Category" };

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column)
             {
                return false;
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(22);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Book Records"));

        add(southPanel, BorderLayout.SOUTH);
        add(scrollPane, BorderLayout.CENTER);

        addBtn.addActionListener(e -> addBook());
        updateBtn.addActionListener(e -> updateBook());
        deleteBtn.addActionListener(e -> deleteBook());
        searchBtn.addActionListener(e -> searchBook());

        viewAllBtn.addActionListener(e -> {
            searchField.setText("");
            viewAll();
        });

        clearBtn.addActionListener(e -> clearFields());
        table.getSelectionModel().addListSelectionListener(e -> {
            int row = table.getSelectedRow(); 

            if (row >= 0) {
                idField.setText(String.valueOf(tableModel.getValueAt(row, 0)));        
                titleField.setText(String.valueOf(tableModel.getValueAt(row, 1)));      
                authorNameField.setText(String.valueOf(tableModel.getValueAt(row, 2))); 
                categoryField.setText(String.valueOf(tableModel.getValueAt(row, 3)));   
            }
        });

        try {
            BookFileIO.createFileIfNotExists(); 
        } catch (IOException ex) 
        {
            showError("Error creating file: " + ex.getMessage());
        }

        viewAll(); 

        setLocationRelativeTo(null); 
        setVisible(true);            
    }

    
    private boolean isValidId(String id) 
    {
        if (id.isEmpty()) 
            {
            showError("Book ID is required!");
            return false;
        }

        if (!id.matches("\\d{8}")) 
            {
            showError("Book ID must be exactly 8 digits (numbers only).\n"
                    + "Minimum: 8 digits, Maximum: 8 digits.");
            return false;
        }
        return true;
    }

   
    private boolean isValidAllFields(String id, String title, String authorName, String category) 
    {
        if (title.isEmpty() || authorName.isEmpty() || category.isEmpty()) {
            showError("All fields are required!");
            return false;
        }

        if (!isValidId(id))
            return false;

        if (title.contains(",") || authorName.contains(",") || category.contains(",")) {
            showError("Commas are not allowed in any field!");
            return false;
        }

        return true; 
    }

    private void addBook()
     {
        String id         = idField.getText().trim();
        String title      = titleField.getText().trim();
        String authorName = authorNameField.getText().trim();
        String category   = categoryField.getText().trim();

        if (!isValidAllFields(id, title, authorName, category))
            return;

        if (BookFileIO.idExists(id)) 
            {
            showError("Duplicate ID! A book with ID " + id + " already exists.");
            return;
        }

        try 
        {
            BookFileIO.addBook(new Book(id, title, authorName, category));
            showInfo("Book added successfully!");
            clearFields();
            viewAll();
        } 
        catch (IOException ex) 
        {
            showError("Error: " + ex.getMessage());
        }
    }

    
    private void updateBook() 
    {
        String id         = idField.getText().trim();
        String title      = titleField.getText().trim();
        String authorName = authorNameField.getText().trim();
        String category   = categoryField.getText().trim();

        if (!isValidAllFields(id, title, authorName, category))
            return;

        try {
            boolean updated = BookFileIO.updateBook(new Book(id, title, authorName, category));

            if (updated) 
                {
                showInfo("Book updated successfully!");
                clearFields();
                viewAll();
            }
             else 
                {
                showError("Book ID not found!");
            }
        } catch (IOException ex) 
        {
            showError("Error: " + ex.getMessage());
        }
    }

    
    private void deleteBook()
     {
        String id = idField.getText().trim();

        if (!isValidId(id))
            return;

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete Book ID: " + id + "?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);

        if (confirm != JOptionPane.YES_OPTION)
            return;

        try {
            boolean deleted = BookFileIO.deleteBook(id);

            if (deleted) 
                {
                showInfo("Book deleted successfully!");
                clearFields();
                viewAll();
            } 
            else 
                {
                showError("Book ID not found!");
            }
        } catch (IOException ex) 
        {
            showError("Error: " + ex.getMessage());
        }
    }

    private void searchBook()
     {
        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            showError("Enter ID or Title to search!");
            return;
        }

        Object[][] results = BookFileIO.searchBooks(keyword);

        tableModel.setRowCount(0); 

        for (int i = 0; i < results.length; i++) 
            {
            tableModel.addRow(results[i]);
            }

        if (results.length == 0)
            showInfo("No matching book found.");
    }

    private void viewAll() 
    {
        Object[][] rows = BookFileIO.getAllBooks();

        tableModel.setRowCount(0); 

        for (int i = 0; i < rows.length; i++)
             {
            if (rows[i][0] != null)
                tableModel.addRow(rows[i]);
        }
    }

    private void clearFields() {
        idField.setText("");
        titleField.setText("");
        authorNameField.setText("");
        categoryField.setText("");
        searchField.setText("");
        table.clearSelection();
    }

    private void showInfo(String msg)
     {
        JOptionPane.showMessageDialog(this, msg, "Info",
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void showError(String msg) 
    {
        JOptionPane.showMessageDialog(this, msg, "Error",
                JOptionPane.ERROR_MESSAGE);
    }
}




