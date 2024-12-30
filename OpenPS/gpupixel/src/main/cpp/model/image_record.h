#pragma once

#include "gpupixel_macros.h"
#include "gpupixel.h"
#include "util.h"
#include <string>

NS_GPUPIXEL_BEGIN

class GPUPIXEL_API ImageRecord : public AbstractRecord {
public:
  const std::string filename;

  explicit ImageRecord(const std::string& filename) : filename(filename) {}

  std::string toString() const override {
    return Util::str_format("filename: %s", filename.c_str());
  }

  bool equals(const AbstractRecord& anotherRecord) const override {
    const ImageRecord* record = dynamic_cast<const ImageRecord*>(&anotherRecord);
    if (!record) {
      return false;
    }
    return filename == record->filename;
  }
};

NS_GPUPIXEL_END