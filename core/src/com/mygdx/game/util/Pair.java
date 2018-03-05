package com.mygdx.game.util;


public class Pair<L,R> {

  private L left;
  private R right;

  public Pair(L left, R right) {
    this.left = left;
    this.right = right;
  }

  public L getKey() { return left; }
  public R getValue() { return right; }
  

public void setLeft(L left) {
	this.left = left;
}

public void setRight(R right) {
	this.right = right;
}

@Override
  public int hashCode() { return left.hashCode() ^ right.hashCode(); }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Pair)) return false;
    Pair pairo = (Pair) o;
    return this.left.equals(pairo.getKey()) &&
           this.right.equals(pairo.getValue());
  }

}