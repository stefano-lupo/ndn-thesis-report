List<String> words = Arrays.asList(" cats", "dogs ", "OWLS", "frogs");
return words.stream()
  .map(String::trim)
  .map(String::toLowerCase)
  .filter(word -> !word.contains("owls"))
  .map(String::length)
  .sorted()
  .collect(Collectors.toList());