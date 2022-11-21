package rr64.developer.infrastructure.api

trait QueryExtractor[Input, Query] {
  def extract(input: Input): Either[String, Query]
}
