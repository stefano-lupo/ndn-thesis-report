List<String> words = Arrays.asList(" cats", "dogs ", "OWLS", "frogs");
  List<Integer> results = new ArrayList<>();
  for (String word : words) {
      word = word.trim();
      word = word.toLowerCase();
      if (!word.contains("owls")) {
          results.add(word.length());
      }
  }
  results.sort(Comparator.naturalOrder());
  return results;