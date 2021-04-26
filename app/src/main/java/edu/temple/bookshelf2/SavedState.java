package edu.temple.bookshelf2;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

public class SavedState implements Parcelable, Serializable {

    private Book selectedBook;
    private Book playingBook;
    private BookList currentList;

    public SavedState(Book selectedBook, Book playingBook, BookList currentList) {
        this.selectedBook = selectedBook;
        this.playingBook = playingBook;
        this.currentList = currentList;
    }


    protected SavedState(Parcel in) {
        selectedBook = in.readParcelable(Book.class.getClassLoader());
        playingBook = in.readParcelable(Book.class.getClassLoader());
        currentList = in.readParcelable(BookList.class.getClassLoader());
    }

    public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
        @Override
        public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
        }

        @Override
        public SavedState[] newArray(int size) {
            return new SavedState[size];
        }
    };

    public Book getSelectedBook() {
        return selectedBook;
    }

    public void setSelectedBook(Book selectedBook) {
        this.selectedBook = selectedBook;
    }

    public Book getPlayingBook() {
        return playingBook;
    }

    public void setPlayingBook(Book playingBook) {
        this.playingBook = playingBook;
    }

    public BookList getCurrentList() {
        return currentList;
    }

    public void setCurrentList(BookList currentList) {
        this.currentList = currentList;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(selectedBook, flags);
        dest.writeParcelable(playingBook, flags);
        dest.writeParcelable(currentList, flags);
    }
}
